echo "Corriendo clientes..."

comps = ("xhgrid5" "xhgrid6")

for (i=0; i < 2; i++)
do
    plink.exe swarch@$comps[$i] -pw swarch -m execClient.sh &
done

wait

scp /client/build/libs/client.jar root@162.168.1.2:/writing/article