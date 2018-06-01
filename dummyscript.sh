#!/bin/sh
for param in "$*"
do
 echo "Voici la liste des paramètres : $param"
done
# Move to the working Dir
cd /Appagent
# Launch the container running the Dummy Agent
if [ $# -eq 2 ]
then
echo "Number of parameters is: $param"
java -cp jade.jar:dummy.jar jade.Boot -container -host localhost -port 1090 -agents 'dummy:com.agents.DummyAg('$1,$2')'
else
echo "The parameters are: $1 $2 $3"
java -cp jade.jar:dummy.jar jade.Boot -container -host localhost -port 1090 -exitwhenempty true -agents 'dummy:com.agents.DummyAg('$1,$2,$3')'
fi