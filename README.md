
# About AgentBPMS project

The main idea of this project is to imitate the behaviors of human resources involved in the execution of business processes.
To do so, we used the agent technology to model and simulate the resource behaviors. 
In its current version the project supports the multi-tenant bonita platform. 
Indeed each tenant involves many users and each user represents an agent. 

The context of use: provide an experimental environment close to the reality in terms of user behaviors in order to evaluate some migration stragies of BMPS tenants from an infrastructure to another.

#### The Agent-based simulator 

the implimented simulator contains two agent types which are as follows:

**1.The AgentUser**: 

This Agent class represents a simple behavior of a human resource involved in the execution of a process: 

  * connect to the BPMS platform
  * retrieve tasks(per page) as well as the total number of tasks within the tenant.
  * Auto assign a task for himself 
  * Execute the task. 
  
**2.The dummy Agent**: 

This agent is used to control the users within a tenant. It can stop and restart agents respectively before and after the migration of a tenant.

#### Simulator Inputs:

The simulator takes as input: 

* The BPMS name
* The BPMS url
* A config.txt: This file has the following format: 
```
The tenantName; userName; password; Number of agents per user; Number of  active Tasks 
```
For example:

```
tenant1;user1;user1;5;1000
```
- The tenant name is written using this regular expression ([A-Z][0-9]{1,4}). For example "T1" to say tenant1. It does not have any relation with the BPMS tenant name, it is named that way to facilitate the management of containers and the organisation of the agents within Jade platform. 

- for the Number of  active Tasks: 
```
If the total number of the current tasks within a tenant is >> parameter of steadiness 
(allowed number of   active tasks per tenant). The agents will close quickly the extra tasks.
```
 ```
If the total number of the current tasks within a tenant is <= parameter of steadiness: 
the agents will not do anything in term of tasks execution. 
```
# Getting Started  

The following instructions will help you running the project as well as creating a doker image for it:

The project is built using maven

```
mvn clean install
```
A docker image of the agent simulator is already created and is publicly available.
The name of the image is: **bpmsagent**
 
In order to execute the docker image, you can use this command:

```
docker run -e BPMSNAME='Name of the BPMS' -e URL='url of the BPMS' -v file_path_to_config.txt:/tmp/config.txt -e CONFIGFILE='/tmp/config.txt' -it bpmsagent
```
To stop and start the agents during migration the following command lines can be used:

```
docker exec -it <Id of the running container>  /bin/bash
cd /Appagent   (represents the working directory)
chmod +x dummyscript.sh  (script shell to start dummyAgent)
```
To stop the agents users the script takes as parameters: a message "stop" and the name of the tenant for example   

```
./dummyscript.sh stop tenant1
```
It allows stopping agents within tenant1 

To start the agents users the script takes as parameters: a message "start", the name of the tenant and the url of the destination platform.
 ```
 ./dummyscript.sh start tenant1 < URL >
  ```
It allows starting agents within tenant1 to reconnect to BPMS using the new URL.
