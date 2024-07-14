#!/bin/bash
rm -f ./server/world/datapacks/comminidatapack*
cp ./comminidatapack ./server/world/datapacks/
docker compose exec mc rcon-cli minecraft:reload
