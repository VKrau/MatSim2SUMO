# MATSim_ZSD
## Немного информации о файлах:
* Links.csv - список линков, на которых поведение агентов будет отслеживаться при симуляции в MATSim
* /src/main/java/org/matsim/run/RunMatsim.java - руннер MATSim
* /SUMO/Routes2SimData.py - позволяет из data.routes.csv создавать файлы simdata (в этих файлах формируются и упорядочиваются данные по агентам и их маршрутам) с данными за определенные часы, например:
`python Routes2SimData.py ["morning",7,9] ["evening",17,19]`

#### В результате будет создано два файла: morning.simdata и evening.simdata, которые содержат данные о поездках агентов в соответствующие часы.
Эти файлы необходимы для генерации агентов и их маршрутов.

* /SUMO/connector.py - руннер симуляции в SUMO

## Запуск симуляции
Т.к. необходимые для SUMO файлы уже были созданы, то достаточно перейти в каталог SUMO и запустить командой:
`python connector.py mySUMOnetwork.net.xml morning.simdata`
или же для запуска симуляции без GUI:
`python connector.py mySUMOnetwork.net.xml morning.simdata --nogui`

после имени скрипта первым аргументом идет имя network-файла, вторым аргументом имя файла simdata