from subprocess import Popen, PIPE


def main():
    # create the engine
    engine = createEngine()

    while True:
        text = engine.stdout.readline().strip()

        if text:
            print(text)


def createEngine():
    JAVA_PATH = "C:/Users/MATTPA~1/Desktop/jdk/jdk-12.0.2/bin/java.exe"
    ENGINE_PATH = "../engine/bin/"
    ENGINE_MAIN_CLASS = "ISMCTS"

    execute_engine = f"{ JAVA_PATH } -cp { ENGINE_PATH } { ENGINE_MAIN_CLASS }"

    return Popen(
        execute_engine, 
        stdin=PIPE,
        stdout=PIPE,
        universal_newlines=True
    )


if __name__ == '__main__':
    main()
