package moe.ofs.addon.aarservice.domains;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DispatchedTanker {

    private String name;

    @SerializedName("runtime_id")
    private int runtimeId;
}
