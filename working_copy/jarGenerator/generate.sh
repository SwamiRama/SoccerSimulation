BIN_PATH="./../../working_copy_ai/bin/"
BIN_PACKAGE_PATH="./SoSi/AIs/"
MAINCLASS_PACKAGE="SoSi.AIs"

CURRENT_PATH="$(pwd)/"

MAIN_SOSI_PATH="-C $(pwd)/../bin ."
RESOURCES="$MAIN_SOSI_PATH/sep/football/ActionHandler.class $MAIN_SOSI_PATH/sep/football/FreePlayActionHandler.class $MAIN_SOSI_PATH/sep/football/KickActionHandler.class $MAIN_SOSI_PATH/sep/football/TickInformation.class $MAIN_SOSI_PATH/sep/football/AI.class $MAIN_SOSI_PATH/sep/football/GameInformation.class $MAIN_SOSI_PATH/sep/football/Position.class $MAIN_SOSI_PATH/SoSi/Debugging/DebuggingAI.class $MAIN_SOSI_PATH/SoSi/Debugging/DebugManager.class $MAIN_SOSI_PATH/SoSi/Model/SoSiPosition.class $MAIN_SOSI_PATH/SoSi/Model/Calculation/Vector2D.class"

clear


# Alte AIs löschen
rm ./AIs-org/*.jar

# JARs erstellen
cd $BIN_PATH
for filename in $(find ./SoSi/AIs/ -name "*.class"  ! -name "*\$*" ! -name "SoSiAI.class") ;
do
    filename_only=$(basename "$filename")
    filename_only="${filename_only%.*}"
    #echo $filename_only

    if [ "$filename_only" = "AiLauncher" ]
    then
      continue;
    fi;

    echo -n "Processing: "
    filelist=""

        for subfilename in $BIN_PACKAGE_PATH$filename_only*.class  ;
        do
		#echo " -> $subfilename"
		echo -n " $(basename "$subfilename")"
		filelist="$filelist $subfilename"
        done;

	if [ "$filename_only" = "FollowingBallAI" ] || [ "$filename_only" = "SimpleGoalScoreAI" ] ;
	then
		filelist="$filelist $BIN_PACKAGE_PATH""TestAI.class"
	fi


	# Additional References
	filelist="$filelist $BIN_PACKAGE_PATH""SoSiAI.class $BIN_PACKAGE_PATH""SoSiAI\$Tuple.class"
    

	echo

	echo -n "Generating jar... "
	jar cfe "$CURRENT_PATH"AIs-org/$filename_only.jar $MAINCLASS_PACKAGE.$filename_only -C $(pwd) $filelist $RESOURCES
	echo "Done."
	echo
	
done;


##############################################################################
echo
echo "### Starting Obfuscation ###"
echo

cd $CURRENT_PATH

rm ./AIs-obfuscated/*.jar

for filename in AIs-org/*.jar ;
do
	java -jar ./proguard4.8/lib/proguard.jar @aiguard.pro -injars $filename -outjars ./AIs-obfuscated/$(basename "$filename")
	echo
done;

echo
echo "### Copy generated files to working_copy/AIs? (Enter y) ### "

read doCopy
if  [ "$doCopy" = "y" ]
then
      echo "Copying generated files"
      cp ./AIs-obfuscated/*.jar ./../AIs/
fi

echo
echo "Everything done."

#jar cf test.jar ./SoSi/Model/AIPkg/BestAI.class
#java -jar ./proguard4.8/lib/proguard.jar @aiguard.pro -injars ./AIs-org/BestAI.jar -outjars ./AIS-obfuscated/BestAI.jar

