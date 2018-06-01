# AgentBPMS

the simulator contains two agent types which are as follows:
- The AgentUser: The behavior of the agent is simple: 

  - connect to the BPMS platform
  - retrieve tasks(per page) as well as the total number of tasks within the tenant.
  - Auto assign a task for himself 
  - Execute the task. 
  
-The dummy Agent: This agent is used to stop/restart AgentUser during the migration Process.

# The simulator takes as input:

- The BPMS name
- The BPMS url
- A config.txt. This file has the following format: The name of the tenant; The tenant id; userName; password; Number of agents per user; Number of  active Tasks. 

     *for the tenant name: 
     
     -it is written this way using this regular expression ([A-Z][0-9]{1,4}). For example "T1" to say tenant1. It does not have any relation with the BPMS tenant name, it is named that way to facilitate the management of containers and the organisation of the agents within Jade platform. 

     *for the Number of  active Tasks: 

-If the total number of the current tasks within a tenant is >> parameter of steadiness (allowed number of active tasks per tenant). The agents will close quickly the extra tasks.
 
- If the total number of the current tasks within a tenant is <= parameter of steadiness: the agents will not do anything in term of tasks execution.  

# A docker image of the agent simulator is created: "The name of the image is: bpmsagent"
 
# For the execution of the docker image, you can use this command:
 
1- docker run -e BPMSNAME='Name of the BPMS' -e URL='url of the BPMS' -v file_path_to_config.txt:/tmp/config.txt -e CONFIGFILE='/tmp/config.txt' -it bpmsagent

# To stop and start the agents during migration the following command line can be used:

1- docker exec -it <Id of the running container>  /bin/bash
  
2- cd /Appagent

3- chmod +x dummyscript.sh   

4- To stop the agents users the script takes as parameters: a message "stop" and the name of the tenant for example
  
  ./dummyscript.sh stop T1 -------------> (stop Agents within T1) 
  
 5- To start the agents users the script takes as parameters: a message "start", the name of the tenant and the url of the destination platform.
 
 ./dummyscript.sh start T1 URL   -------------> (start Agents within T1 to reconnect to the new URL).
  




 
 
