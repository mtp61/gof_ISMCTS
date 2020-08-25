import socketio
import copy
import json
from subprocess import Popen, PIPE
from signal import signal, SIGINT
from sys import argv, exit


# constants
JAVA_PATH = "C:/Users/MATTPA~1/Desktop/jdk/jdk-12.0.2/bin/java.exe"
ENGINE_PATH = "../engine/bin/"
ENGINE_MAIN_CLASS = "ISMCTS"

USERNAME_DEFAULT = "big_bot"
GAME_NAME_DEFAULT = "test"

MAX_ITR_DEFAULT = 10000
MAX_TIME_DEFAULT = 5

URL = "http://localhost:3331/"


class Client:
    def main(self):
        # set up kill function
        signal(SIGINT, self.kill)

        # check args
        if len(argv) == 1:
            self.username = USERNAME_DEFAULT
            self.game_name = GAME_NAME_DEFAULT
        elif len(argv) == 3:
            self.username = argv[1]
            self.game_name = argv[2]
        else:
            raise Exception("Bad number of args")
        
        self.MAX_ITR = MAX_ITR_DEFAULT
        self.MAX_TIME = MAX_TIME_DEFAULT

        # connect to the server
        self.sio = socketio.Client()
        self.sio.connect(URL + self.game_name)

        # connect to game and ready up
        self.sio.emit('game_connection', (self.game_name, self.username))
        self.sendMessage("!ready")

        # setup variables
        self.engine_active = False     
        self.cards_played = []
        self.old_game_state = None

        # process new game states
        @self.sio.on('game_state')
        def onGameState(game_state):
            # if game active
            if game_state['active']:
                # if new game state
                if json.dumps(game_state) != json.dumps(self.old_game_state):
                    # if new cards
                    if self.old_game_state == None or json.dumps(game_state['current_hand']) != json.dumps(self.old_game_state['current_hand']):
                        # add new cards to cards played
                        for card in game_state['current_hand']:
                            self.cards_played.append(card)

                    # update old game state
                    self.old_game_state = copy.deepcopy(game_state)

                # check if we have an action todo add timer to not double respond
                if game_state['to_play'] == self.username:
                    if not self.engine_active:
                        # start engine
                        print(self.engineArgs(game_state))
                        self.createEngine(self.engineArgs(game_state))

                        self.engine_active = True

        while True:
            if self.engine_active:
                text = self.engine.stdout.readline().strip()
                if text:
                    if len(text) >= 6 and text[:6] == "action":  # we have a viable action
                        print(text)

                        # send the message with the info
                        self.sendMessage(self.engineToMessage(text[7:]))

                        # kill current engine
                        self.engine.kill()
                        self.engine_active = False
                    else:  # just info text
                        print(f"info { text }")
                        
    

    def engineArgs(self, game_state):
        """
        all int[] are in the format [19,12,3,4]

        int max_itr
        int max_time
        int[] player1_cards
        int[] card_counts
        int[] cards_played
        int[] current_hand
        int num_passes
        int player_to_act
        """

        # max itr and max time
        arg_string = f"{ self.MAX_ITR } { self.MAX_TIME } "

        # player 1 cards
        arg_string += self.listToArg([self.cardToNum(c) for c in game_state['player_cards'][self.username]]) + ' '

        # card counts
        # todo order needs to start with the bot, currently only works if bot joins first
        bot_index = game_state['players'].index(self.username)
        player_order = []
        for i in range(4):
            player_order.append(game_state['players'][(bot_index + i) % 4])

        arg_string += self.listToArg([int(game_state['num_cards'][p]) for p in player_order]) + ' '

        # cards played
        arg_string += self.listToArg([self.cardToNum(c) for c in self.cards_played]) + ' '

        # current hand
        arg_string += self.listToArg([self.cardToNum(c) for c in game_state['current_hand']]) + ' '

        # num pass and player to act
        arg_string += f"{ int(game_state['num_passes']) } { game_state['players'].index(game_state['to_play']) + 1 }"

        return arg_string
    

    def engineToMessage(self, engine_text):
        if engine_text == "0":  # if pass
            return "!play"

        engine_cards = [self.numToCardStr(int(c)) for c in engine_text.split(" ")]
        message_text = "!play"
        for c in engine_cards:
            message_text += ' ' + c
        return message_text


    def numToCardStr(self, num):
        card_str = str(num // 10)
        if num % 10 == 0:
            card_str += 'g'
        elif num % 10 == 1:
            card_str += 'y'
        elif num % 10 == 2:
            card_str += 'r'
        elif num % 10 == 3:
            card_str += 'm'
        return card_str

    def cardToNum(self, card):  # converts a card json object to the single number used by the engine
        card_num = 10 * int(card['value'])
        if card['color'] == 'y':
            card_num += 1
        elif card['color'] == 'r':
            card_num += 2
        elif card['color'] == 'm':
            card_num += 3
        return card_num


    def listToArg(self, l):  # converts a list to the engine arg
        if len(l) == 0:
            return "[]"
        
        arg_str = "["
        for e in l:
            arg_str += str(e) + ','
        return arg_str[:-1] + ']'


    def createEngine(self, engine_args):
        execute_engine = f"{ JAVA_PATH } -cp { ENGINE_PATH } { ENGINE_MAIN_CLASS }"

        if len(engine_args) > 0:
            execute_engine += " " + engine_args

        self.engine = Popen(
            execute_engine, 
            stdin=PIPE,
            stdout=PIPE,
            universal_newlines=True
        )

        self.engine_active = True


    def sendMessage(self, message):
        self.sio.emit('chat-message', (self.game_name, self.username, message))


    def kill(self, signal_received, frame):
        # kill the engine
        if self.engine_active:
            self.engine.kill()

        # disconnect from the server
        self.sio.disconnect()

        # print that program was hard killed
        print('Program killed')

        # kill the program
        exit()
    

if __name__ == '__main__':
    # instantiate the client class
    client = Client()

    # run the main method
    client.main()
