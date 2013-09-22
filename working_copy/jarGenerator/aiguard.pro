-libraryjars  <java.home>/lib/rt.jar

-keep public interface sep.football.*
-keep public interface SoSi.Debugging.DebuggingAI
-keep public interface SoSi.Debugging.DebugManager

-keepclassmembers public interface SoSi.Debugging.DebuggingAI {
    public *;
}

-keepclassmembers public interface SoSi.Debugging.DebugManager {
    public *;
}

-keepclassmembers public interface sep.football.* {
    public *;
}

-keep public class * implements sep.football.AI {
    public void kickOff(sep.football.GameInformation, sep.football.TickInformation, sep.football.KickActionHandler);
    public void freeKick(sep.football.GameInformation, sep.football.TickInformation, sep.football.KickActionHandler);
    public void freePlay(sep.football.GameInformation, sep.football.TickInformation, sep.football.FreePlayActionHandler);
}

-keep public class * implements SoSi.Debugging.DebuggingAI {
    public void kickOff(sep.football.GameInformation, sep.football.TickInformation, sep.football.KickActionHandler);
    public void freeKick(sep.football.GameInformation, sep.football.TickInformation, sep.football.KickActionHandler);
    public void freePlay(sep.football.GameInformation, sep.football.TickInformation, sep.football.FreePlayActionHandler);
    public void print(java.lang.String);
}

-keep public class * extends SoSi.AIs.SoSiAI {
    public void kickOff(sep.football.GameInformation, sep.football.TickInformation, sep.football.KickActionHandler);
    public void freeKick(sep.football.GameInformation, sep.football.TickInformation, sep.football.KickActionHandler);
    public void freePlay(sep.football.GameInformation, sep.football.TickInformation, sep.football.FreePlayActionHandler);
    public void print(java.lang.String);
}