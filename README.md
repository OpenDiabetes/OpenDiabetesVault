# OpenDiabetesVault
Wrap up project for convenient checkout of all needed repositories.
References to submodules are bound to a working state of the project.

## Clone the Project Code
Because we are using submodules you have to init the submodules after the first clone.

```
git clone git@github.com:OpenDiabetes/OpenDiabetesVault.git
git submodule update --init
```

## Update the Project Code
Because we are using submodulues you have to update the submodules after you pulled a new status of the project.

```
git pull
git submodule update 
```

## Build the Project
To build the project we provide a ANT script to compile a single jar file.
For compiling you need the java compiler 1.8.0 and ANT XXX.

```
ant TODO
```

Additionally, for the plotting script you nee Python 2.7.12 with mathplotlib 2.0.2 and pyinstaller 3.2.1. 

```
pip install mathplotlib
pip install pyinstaller
cd plot
pyinstaller plot.py --clean
cp ./config.json ./dist/plot/
```

## Run the Program
After compiling the code you can start the program by using the command:

```
java -jar OpendiabetesVault.jar
```

## Getting Started!
To get started using OpenDiabetesVault or contribute to the project, please visit our Wiki pages:
* [Getting Started](https://github.com/OpenDiabetes/OpenDiabetesVault/wiki/Getting-Started!)
* [Contribute](https://github.com/OpenDiabetes/OpenDiabetesVault/wiki/Contribute)
