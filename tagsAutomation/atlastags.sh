#!/bin/bash
if [[ $# -lt 2 ]]; then
    echo "$0 {User Name} {Password}"
    exit 0
fi

## run addtag MVP1
java -jar tagging.jar add p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 TEST test_atlas_tags_automation.xlsx 

## run addtag MostUsedList
java -jar tagging.jar add p01eaedl azalvedlmstdp07.p01eaedl.manulife.com 21443 $1 $2 TEST "D:/Local.xlsx"

##END
