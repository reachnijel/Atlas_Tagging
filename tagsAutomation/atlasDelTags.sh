#!/bin/bash
if [[ $# -lt 3 ]]; then
    echo "$0 {User Name} {Password} {Schema_Name}"
    exit 0
fi

## remove tag from schema
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 VN $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 AMS $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 CAS $3

java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Regulated_PHI $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Regulated_PIFI $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Regulated_PII $3

java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Restricted $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Public $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Internal $3

## java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Actuary $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Customer $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Distributor $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Employee $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Finance $3
java -jar tagging.jar deltag p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 Product $3

##END
