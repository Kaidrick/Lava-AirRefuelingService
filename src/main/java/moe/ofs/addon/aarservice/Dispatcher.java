package moe.ofs.addon.aarservice;

import com.google.gson.Gson;
import moe.ofs.addon.aarservice.domains.BriefedWaypoint;
import moe.ofs.addon.aarservice.domains.CustomPattern;
import moe.ofs.addon.aarservice.domains.DispatchedTanker;
import moe.ofs.addon.aarservice.domains.TankerService;
import moe.ofs.backend.domain.ExportObject;
import moe.ofs.backend.function.coordoffset.Offset;
import moe.ofs.backend.function.unitconversion.Coordinates;
import moe.ofs.backend.handlers.*;
import moe.ofs.backend.logmanager.Logger;
import moe.ofs.backend.object.*;
import moe.ofs.backend.object.command.ActivateBeacon;
import moe.ofs.backend.object.command.ActivateBeaconParams;
import moe.ofs.backend.object.command.BeaconSystem;
import moe.ofs.backend.object.command.BeaconType;
import moe.ofs.backend.object.tasks.*;
import moe.ofs.backend.object.tasks.enroute.Tanker;
import moe.ofs.backend.object.tasks.enroute.TankerParams;
import moe.ofs.backend.object.tasks.main.Orbit;
import moe.ofs.backend.object.tasks.main.OrbitParams;
import moe.ofs.backend.repositories.ExportObjectRepository;
import moe.ofs.backend.request.server.ServerActionRequest;
import moe.ofs.backend.request.server.ServerDataRequest;
import moe.ofs.backend.services.MissionDataService;
import moe.ofs.backend.services.ParkingInfoService;
import moe.ofs.backend.util.LuaScripts;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class Dispatcher {

    private final ExportObjectRepository exportObjectRepository;

    private ScheduledExecutorService tankerManageExecutorService;

    private Set<TankerStateWrapper> stateWrapperSet = new HashSet<>();

    private final ParkingInfoService parkingInfoService;

    private final MissionDataService<DispatchedTanker> dispatchedTankerMissionDataService;

    public Dispatcher(ExportObjectRepository exportObjectRepository, ParkingInfoService parkingInfoService,
                      MissionDataService<DispatchedTanker> dispatchedTankerMissionDataService) {

        this.exportObjectRepository = exportObjectRepository;
        this.parkingInfoService = parkingInfoService;
        this.dispatchedTankerMissionDataService = dispatchedTankerMissionDataService;

        tankerManageExecutorService = Executors.newSingleThreadScheduledExecutor();


//        MissionStartObservable missionStartObservable = theater ->
//                tankerManageExecutorService.scheduleWithFixedDelay(serviceMonitorStep,
//                        1, 10, TimeUnit.SECONDS);
//        missionStartObservable.register();

//        GuiServerResetObservable resetObservable = () -> tankerManageExecutorService.shutdownNow();
//        resetObservable.register();
//
//        ControlPanelShutdownObservable shutdownObservable = () -> tankerManageExecutorService.shutdownNow();
//        shutdownObservable.register();

        BackgroundTaskRestartObservable restartObservable = () -> stateWrapperSet.clear();
        restartObservable.register();
    }




    public void monitor(TankerStateWrapper wrapper) {
        // if tanker is near mission mix for the first time, flag initiated
        Unit tanker = wrapper.getService().getUnit();
        // find current position of the tanker
        Optional<ExportObject> tankerObjectOptional = exportObjectRepository.findByRuntimeID((long) tanker.getId());
        if(tankerObjectOptional.isPresent()) {
            ExportObject tankerData = tankerObjectOptional.get();

            TankerService tankerService = wrapper.getService();

            if(!wrapper.isInitiated()) {
                // check distance to holding fix
                BriefedWaypoint holdingFix = tankerService.getHoldingFix();

                Vector3D currentPosition = tankerData.getPosition();
                Vector3D holdingFixPosition = Coordinates.convertToVector3D(
                        holdingFix.getNavFix().getPosition());
                holdingFixPosition.setY(holdingFix.getAssignedAltitude());

                // slant range is better
                double distance = Offset.slantRange(currentPosition, holdingFixPosition);
                System.out.println("tanker distance to holding fix = " + distance);
            }

        } else {
            System.out.println("no data for " + tanker.getId());
        }

    }

    public Group buildMissionGroup(TankerService tankerService) {

        ParkingInfo parking = parkingInfoService.getAllParking().stream()
                .filter(parkingInfo -> parkingInfo.getAirdromeId() == tankerService.getStartingAirdromeId())
                .filter(parkingInfo -> parkingInfo.getTerminalType() == 104)
                .findAny().orElseThrow(RuntimeException::new);

        Group.GroupBuilder groupBuilder = new Group.GroupBuilder();
        Unit.UnitBuilder unitBuilder = new Unit.UnitBuilder();

        Payload payload = new Payload();
        payload.setChaff(0);
        payload.setFlare(0);
        payload.setFuel(90700);
        payload.setGun(0);
        payload.setPylons(new HashMap<>());

        moe.ofs.backend.object.Route route = new moe.ofs.backend.object.Route();
        Point startWaypoint = new Point.PointBuilder()
                .setAction(Point.Action.FROM_RUNWAY)
                .setAirdromeId(parking.getAirdromeId())
                .setType(Point.PointType.TakeOff)
                .setPos(parking.getPosition().getX(), parking.getPosition().getZ())
                .setAlt(parking.getPosition().getY())
                .build();

        route.addPoint(startWaypoint);

        // convert service route to object.Route
        tankerService.getRoute().getBriefedWaypoints().forEach(wp -> {
            // convert LL to LO first
            Vector3D vec3 = Coordinates.convertToVector3D(wp.getNavFix().getPosition());

            Point.PointBuilder builder = new Point.PointBuilder();

            Point point = builder.setX(vec3.getX()).setY(vec3.getZ()).setAlt(wp.getAssignedAltitude())
                    .setAction(Point.Action.TURNING_POINT)
                    .setType(Point.PointType.TurningPoint)
                    .setAltType(Point.AltType.BARO)
                    .setSpeed(wp.getAssignedSpeed())
                    .build();

            Point nextPoint = null;
            if(wp.equals(tankerService.getHoldingFix())) {
                // set this point for orbit holding fix
                ComboTask comboTask = new ComboTask();
                ComboTaskParams comboTaskParams = new ComboTaskParams();
                List<Task> taskList = new ArrayList<>();

                Tanker tankerTask = new Tanker();
                tankerTask.setAuto(false);
                tankerTask.setEnabled(true);
                tankerTask.setParams(new TankerParams());
                tankerTask.setNumber(1);

                Orbit orbit = new Orbit();
                OrbitParams orbitParams = new OrbitParams();

                // TODO --> pattern and altitude can be overridden
                orbitParams.setAltitude(tankerService.getHoldingFix().getAssignedAltitude());
                orbitParams.setPattern("Race-Track");
                orbitParams.setSpeed(tankerService.getHoldingFix().getAssignedSpeed());
                orbit.setParams(orbitParams);
                orbit.setAuto(false);
                orbit.setEnabled(true);
                orbit.setNumber(2);

                ActivateBeacon activateBeaconCommand = new ActivateBeacon();
                ActivateBeaconParams activateBeaconParams = new ActivateBeaconParams();
                activateBeaconParams.setAirborne(true);
                activateBeaconParams.setCallsign("TKR");
                activateBeaconParams.setBearing(true);
                activateBeaconParams.setChannel(1);
                activateBeaconParams.setModeChannel("X");
                activateBeaconParams.setFrequency(1088000000);
                activateBeaconParams.setSystem(BeaconSystem.TACAN_TANKER.getType());
                activateBeaconParams.setType(BeaconType.BEACON_TYPE_TACAN.getType());

                activateBeaconCommand.setParams(activateBeaconParams);

                WrappedAction wrappedAction = new WrappedAction();
                WrappedActionParams wrappedActionParams = new WrappedActionParams();
                wrappedActionParams.setAction(activateBeaconCommand);
                wrappedAction.setParams(wrappedActionParams);
                wrappedAction.setAuto(false);
                wrappedAction.setEnabled(true);
                wrappedAction.setNumber(3);

                taskList.add(tankerTask);
                taskList.add(orbit);
                taskList.add(wrappedAction);

                comboTaskParams.setTasks(taskList);

                comboTask.setParams(comboTaskParams);

                point.setTask(comboTask);

                // check for custom pattern
                CustomPattern customPattern;
                if((customPattern = tankerService.getCustomPattern()) != null) {
                    Vector3D nextVec3 = Offset.of(customPattern.getPatternLegLength(),
                            Math.toRadians(customPattern.getPatternInbound()),
                            vec3);

                    nextPoint = new Point.PointBuilder().setX(nextVec3.getX()).setY(nextVec3.getZ())
                            .setAlt(customPattern.getPatternAltitude())
                            .setAction(Point.Action.TURNING_POINT)
                            .setType(Point.PointType.TurningPoint)
                            .setAltType(Point.AltType.BARO)
                            .setSpeed(tankerService.getHoldingFix().getAssignedSpeed())
                            .build();
                }
            }

            route.addPoint(point);

            if(nextPoint != null) {
                route.addPoint(nextPoint);
            }
        });


        Unit unit = unitBuilder.setParking(parking.getParkingId())
                .setPos(parking.getPosition().getX(), parking.getPosition().getZ())
                .setCategory(Unit.Category.AIRPLANE)
                .setSkill(Unit.Skill.HIGH)
                .setType(tankerService.getAircraftType())
                .setPayload(payload)
                .setName(tankerService.getTankerMissionName()).build();

        tankerService.setUnit(unit);

        List<Unit> units = new ArrayList<>();
        units.add(unit);

        return groupBuilder.setUnits(units)
                .setCategory(Group.Category.AIRPLANE)
                .setName(tankerService.getTankerMissionName())
                .setRoute(route)
                .build();
    }

    // spawn aircraft based on theater name and predefined airport and routes

    // dispatcher periodically check the status of tanker
    // if tanker is stationary for too long, destroy tanker
    // if a tanker is destroyed (despawn observable), re-dispatch
    // if a tanker is low on fuel, call rtb
    // if a tanker is manually recalled, unregister
    // if a tanker is manually destroyed, unregister
    public void resumeDispatch(TankerService tankerService, DispatchedTanker dispatchedTanker) {
        tankerService.getUnit().setId(dispatchedTanker.getRuntimeId());

        // creates link
        stateWrapperSet.add(new TankerStateWrapper(tankerService, dispatchedTanker));

        System.out.println(tankerService + " / " + dispatchedTanker);
    }

    public void terminateDispatch(TankerService service) {
        // destroy tanker immediately
        new ServerActionRequest(String.format("Unit.destroy({ id_ = %d })", service.getUnit().getId())).send();


        // dispose and remove wrapper
        Optional<TankerStateWrapper> optional = stateWrapperSet.stream()
                .filter(wrapper -> wrapper.getService().equals(service)).findAny();

        if(optional.isPresent()) {
            TankerStateWrapper wrapper = optional.get();

            dispatchedTankerMissionDataService.deleteBy("runtime_id",
                    (long) wrapper.getDispatchedTanker().getRuntimeId());

            wrapper.dispose();
            stateWrapperSet.remove(wrapper);
        }

    }

    public void dispatch(TankerService tankerService) {

        if(tankerService == null)
            return;

        if(tankerService.getUnit() == null)
            buildMissionGroup(tankerService);

        Gson gson = new Gson();

        Group group = buildMissionGroup(tankerService);

        ServerDataRequest request = new ServerDataRequest(
                LuaScripts.loadAndPrepare("spawn_control/spawn_group.lua", gson.toJson(group))
        );


        SpawnInfo info = request.getAs(SpawnInfo.class);

        Unit unit = tankerService.getUnit();

        unit.setId(info.getUnits().get(unit.getName()));  // id is not reliable; reverse search is needed

        DispatchedTanker dispatchedTanker = new DispatchedTanker();
        dispatchedTanker.setRuntimeId(unit.getId());
        dispatchedTanker.setName(unit.getName());

        dispatchedTankerMissionDataService.save(dispatchedTanker);

        resumeDispatch(tankerService, dispatchedTanker);

        Logger.addon("Tanker Service Dispatched: " + unit + " with Route: " + tankerService.getRoute());


        // TODO: add some code to track the status of the aircraft
        // if it is at mission area, add a radio menu?
        // if it is shutdown / destroyed, add a new group immediately
        // if it is out of fuel /
    }
}
