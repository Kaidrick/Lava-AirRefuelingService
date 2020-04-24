package moe.ofs.addon.aarservice.domains;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispatchedTanker {

    private String name;

    @SerializedName("runtime_id")
    private int runtimeId;
}
