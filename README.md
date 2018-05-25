# AgentBPMS

The simulator takes as input:

- The BPMS name
- The BPMS url
- A config.txt file that contains information about the tenants, their ids and number of users within each one. 
- The allowed number of tasks within tenants (parameter of steadiness)

the simulator contains one agent type which is the AgentUser.

The behavior of the agent is simple: 

  - connect to the BPMS platform
  - retrieve tasks(per page) as well as the total number of tasks within the tenant.
  - Auto assign a task for himself 
  - Execute the task. 
  
 If the total number of the current tasks within a tenant is >> parameter of steadiness (allowed number of tasks per tenant).
 The agents will close quickly the extra tasks.
 
If the total number of the current tasks within a tenant is <= parameter of steadiness: the agents will not do anything in term of tasks execution.  
