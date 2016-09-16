
aws cloudformation create-stack  \
  --template-body file://bamboo.json \
  --stack-name bamboo-cluster \
  --parameters \
    ParameterKey=KeyName,ParameterValue= \
    ParameterKey=ExhibitorDiscoveryUrl,ParameterValue= \
    ParameterKey=ZkClientSecurityGroup,ParameterValue= \
    ParameterKey=VpcId,ParameterValue=vpc-78db5a1c \
    ParameterKey=Subnets,ParameterValue= \
    ParameterKey=AdminSecurityGroup,ParameterValue= \
    ParameterKey=BambooDockerImage,ParameterValue=thefactory/bamboo:latest \
    ParameterKey=ClusterSize,ParameterValue=1 \
    ParameterKey=InstanceAmi,ParameterValue= \
    ParameterKey=MarathonEndpoint,ParameterValue=
