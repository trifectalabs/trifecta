aws cloudformation create-stack \
  --template-body file://cassandra-seeds.json \
  --stack-name cassandra-seed-cluster \
  --parameters \
    ParameterKey=KeyName,ParameterValue=\
    ParameterKey=VpcId,ParameterValue= \
    ParameterKey=Subnets,ParameterValue= \
    ParameterKey=AdminSecurityGroup,ParameterValue= \
    ParameterKey=ClusterSize,ParameterValue=3 \
    ParameterKey=InstanceAmi,ParameterValue=
