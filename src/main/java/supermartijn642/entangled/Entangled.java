package supermartijn642.entangled;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;

@Mod(modid = Entangled.MODID,name = Entangled.NAME,version = Entangled.VERSION,acceptedMinecraftVersions = Entangled.MC_VERSIONS)
public class Entangled {

    public static final String MODID = "entangled";
    public static final String NAME = "Entangled";
    public static final String MC_VERSIONS = "[1.12.2]";
    public static final String VERSION = "1.1.1";

    @SidedProxy(clientSide = "supermartijn642.entangled.ClientProxy", serverSide = "supermartijn642.entangled.CommonProxy")
    public static CommonProxy proxy;

}
