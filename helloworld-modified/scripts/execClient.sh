cd Desktop/EspinosaNinoCallback || exit

client(){
  echo "1000000"
  sleep 120
  echo "exit"
}

client | java -jar client.jar >> "client.log"