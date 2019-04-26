# Presto UDF | Gradle | Docker

The framework can be used to write, build and test User Defined Functions for Presto locally. I am using gradle file to build the jar. Find the steps below to build and test the UDF written here.
- Clone the repo
  ``` gradle clean build ```

Once the build is complete, execute the below commands (from the folder where the make file exists)
- ```  cd docker-presto-cluster ```
- ``` make all ```
- ``` make run-with-logs ``` If you want to see the logs which will show that your UDF is loaded

### Credits
The docker build presto cluster is from  [Lewuathe](https://github.com/Lewuathe)/**[docker-presto-cluster](https://github.com/Lewuathe/docker-presto-cluster)** 
The docker-compose.template file is different from the one present in the above repo.
