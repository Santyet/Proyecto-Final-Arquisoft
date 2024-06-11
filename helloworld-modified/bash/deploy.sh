#!/bin/bash

echo "Iniciando deploy"


for i in 2 6 7 11 13
do

  pscp.exe -pw swarch -r ../../../Proyecto-Final-Arquisoft swarch@xhgrid${i}:/home/swarch/Documents/brian_brawn_pablo_santiago

  #plink.exe -ssh swarch@xhgrid${my_array[$i]} -pw swarch -m integrales/bash/runBuild.sh
  #pscp.exe -pw swarch distributed_integrals/configSwarch/${my_array[$i]}/config.sub  swarch@xhgrid${my_array[$i]}:/home/swarch/Documents/brian_brawn_pablo_santiago/integrales/worker/build/libs
  #plink.exe swarch@xhgrid${my_array[$i]} -pw swarch "cd Documents/brian_brawn_pablo_santiago/integrales/worker/build/libs/;jar uf worker.jar config.sub"
done