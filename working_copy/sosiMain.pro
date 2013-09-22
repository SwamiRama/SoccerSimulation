-injars       SoSi.jar
-outjars      SoSi_obfuscated.jar
-libraryjars  <java.home>/lib/rt.jar
-printmapping myapplication.map

-keep public class SoSi.View.Launcher {
    public static void main(java.lang.String[]);
    }

