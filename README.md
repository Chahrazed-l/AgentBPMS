# AgentBPMS
The simulator takes as input:
- The BPMS name
- The BPMS url
- A config.txt file that contains information about the tenants, their ids and number of users within each one. 
- The number of total tasks within a tenant (parameter of steadiness)

the simulator contains one agent type which is the AgentUser.
The behavior of the agent is simple: 
  1- connect to the BPMS platform
  2- retrieve tasks(per page) as well as the total number of tasks within the tenant.
  3- Auto assign a task for himself 
  4- Execute the task. 
 If the total number of the current tasks within a tenant is >> parameter of steadiness (allowed number of tasks per tenant).
 The agent will close quickly the extra tasks.
