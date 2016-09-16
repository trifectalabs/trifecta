aws cloudformation create-stack \
    --template-body file://docker-registry.json \
    --stack-name docker-registry \
    --capabilities CAPABILITY_IAM \
    --parameters \
        ParameterKey=KeyName,ParameterValue= \
        ParameterKey=RegistryAuth,ParameterValue='' \
        ParameterKey=S3Bucket,ParameterValue= \
        ParameterKey=AvailabilityZones,ParameterValue=us-east-1a \
        ParameterKey=SslCertificate,ParameterValue= \
        ParameterKey=DnsPrefix,ParameterValue= \
        ParameterKey=DnsZone,ParameterValue= \
        ParameterKey=VpcId,ParameterValue= \
        ParameterKey=Subnets,ParameterValue= \
        ParameterKey=AdminSecurityGroup,ParameterValue=
