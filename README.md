### File details: 
**ReadPacketFile**: pcap4j library opensource code to read pcap files


**process_string**: Calculates the interarrival time between two packets irrespective of any Keyby.

**process_stringIP**: keyby IP and calculates average interarrival time. 

**CountTime2**: IP is string and uses CustomPair2

**CustomPair2**: IP is string

**process_stringIP_Port**: keyby's IP-Port combination and processes using CountTime3.

**CountTime3**: IP_Port is there and uses CustomPair3

**CustomPair3**: IP_Port is a field

**process_stringIP_Port_currentTime**: Same as process_stringIP_Port. Only change is it uses currentTime which is being set in CountCurrentTimewise

**CountCurrentTimeWise3**: setting timestap as System.nanos

**process_stringIP_Port_randomTime**: We set timestamp with random values in this function-> in the readpacketfile code part.

**aggregate.java** : Takes the output of process functions as input and summarizes the statistics


### Way to Execute: 
We explain executing process_string. Other process functions follow the same pattern.
1. Import project to IntelliJ
2. In process_string function, the following details have to be set:
	a. In PCAP_FILE String, give the pcap file path.
	b. In StreamingFileSink variable, the output path has to be set.
3. If there is any error in running, then Edit run configuration of IntelliJ -> modify options -> include dependencies with Provided scope
4. The output file is stored in the path set in 2b. This file has to be copied and pasted in the input.txt file in src/main/resources folder. The aggregate.java function takes input from this file and prints the overall aggregation results on the console.
