import copy
import math
import numpy as np
import random
from termcolor import colored  # can be taken out if you don't like it...

# # # # # # # # # # # # # # global values  # # # # # # # # # # # # # #
ROW_COUNT = 6
COLUMN_COUNT = 7

RED_CHAR = colored('X', 'red')  # RED_CHAR = 'X'
BLUE_CHAR = colored('O', 'blue')  # BLUE_CHAR = 'O'

EMPTY = 0
RED_INT = 1
BLUE_INT = 2


# # # # # # # # # # # # # # functions definitions # # # # # # # # # # # # # #

def create_board():
    """creat empty board for new game"""
    board = np.zeros((ROW_COUNT, COLUMN_COUNT), dtype=int)
    return board


def drop_chip(board, row, col, chip):
    """place a chip (red or BLUE) in a certain position in board"""
    board[row][col] = chip


def is_valid_location(board, col):
    """check if a given column in the board has a room for extra dropped chip"""
    return board[ROW_COUNT - 1][col] == 0

def get_next_open_row(board, col):
    """assuming column is available to drop the chip,
    the function returns the lowest empty row  """
    for r in range(ROW_COUNT):
        if board[r][col] == 0:
            return r

def print_board(board):
    """print current board with all chips put in so far"""
    # print(np.flip(board, 0))
    print(" 1 2 3 4 5 6 7 \n" "|" + np.array2string(np.flip(np.flip(board, 1)))
          .replace("[", "").replace("]", "").replace(" ", "|").replace("0", "_")
          .replace("1", RED_CHAR).replace("2", BLUE_CHAR).replace("\n", "|\n") + "|")

def game_is_won(board, chip):
    """check if current board contain a sequence of 4-in-a-row of in the board
     for the player that play with "chip"  """

    winning_Sequence = np.array([chip, chip, chip, chip])
    # Check horizontal sequences
    for r in range(ROW_COUNT):
        if "".join(list(map(str, winning_Sequence))) in "".join(list(map(str, board[r, :]))):
            return True
    # Check vertical sequences
    for c in range(COLUMN_COUNT):
        if "".join(list(map(str, winning_Sequence))) in "".join(list(map(str, board[:, c]))):
            return True
    # Check positively sloped diagonals
    for offset in range(-2, 4):
        if "".join(list(map(str, winning_Sequence))) in "".join(list(map(str, board.diagonal(offset)))):
            return True
    # Check negatively sloped diagonals
    for offset in range(-2, 4):
        if "".join(list(map(str, winning_Sequence))) in "".join(list(map(str, np.flip(board, 1).diagonal(offset)))):
            return True

def get_valid_locations(board):
    valid_locations = []
    for col in range(COLUMN_COUNT):
        if is_valid_location(board, col):
            valid_locations.append(col)
    return valid_locations

def MoveRandom(board, color):
    valid_locations = get_valid_locations(board)
    column = random.choice(valid_locations)   # you can replace with input if you like... -- line updated with Gilad's code-- thanks!
    row = get_next_open_row(board, column)
    drop_chip(board, row, column, color)

def score_area(boardsection,chip):
    score = 0
    opponent = BLUE_INT
    # we just want to figure out the perspective of a negative opponent 
    if chip == BLUE_INT:
        opponent = RED_INT
    # negatively score the section when it has trouble written all over it
    if boardsection.count(opponent) == 3 and boardsection.count(chip) == 1:
        score = score - 4 
    #elite section find
    if boardsection.count(chip) == 4:
        score = score + 100
    #favorable spots for us to win
    elif boardsection.count(chip) == 3 and boardsection.count(0) == 0:
        score = score + 5
    #semi favorable spots for us to win
    elif boardsection.count(chip) == 2  and boardsection.count(0) == 0:
        score = score + 2
    
    # give the crossection score
    return score
# give the score of the board
def score_position(board, piece):

    score = 0

    # valueing the cneter board the highest
    center = [int(i) for i in list(board[:,3])]
    center_c = center.count(piece)
    score += (center_c * 6) 

    # below we go over every single window in different directions and adding up their values to the score
    # score horizonte
    for row in range(ROW_COUNT):
        row_array = [int(i) for i in list(board[row,:])]
        for column in range(COLUMN_COUNT - 3):
            boardsection = row_array[column:column + 4]
            score += score_area(boardsection, piece)

    # score up
    for column in range(COLUMN_COUNT):
        col_array = [int(i) for i in list(board[:,column])]
        for row in range(ROW_COUNT-3):
            boardsection = col_array[row:row+4]
            score += score_area(boardsection, piece)

    # score up diagonal
    for row in range(3,ROW_COUNT):
        for column in range(COLUMN_COUNT - 3):
            window = [board[row-diag][column+diag] for diag in range(4)]
            score += score_area(window, piece)

    # score down diagonal
    for row in range(3,ROW_COUNT):
        for column in range(3,COLUMN_COUNT):
            boardsection = [board[row-diag][column-diag] for diag in range(4)]
            score += score_area(boardsection, piece)

    return score
# check if the game is won or if there is nowhere to do anything
def is_final(board_copy):
    return game_is_won(board_copy,RED_INT) or game_is_won(board_copy,BLUE_INT) or len(get_valid_locations(board_copy)) == 0
# Using the min max algorithm trying to maximize the moves of the AI, Red_Int
# we go here 5 depth in meaning we check 5 recursions levels down for the best path
# Alpha and beta pruning are used to calculate based on if the opponent will make best opposing move
# Here as seen we are maximizing the AI as Red_Int to win
def minimax(board, depth, alpha, beta, maximizing_player):

    
    valid_locations = get_valid_locations(board)

    # is this board finished
    final = is_final(board)

    if depth == 0 or final:
        if final: # winning  
            if game_is_won(board,RED_INT):
                # basically won
                return (None, 10000000)
            elif game_is_won(board,BLUE_INT):
                # blue wins in this
                return (None, -10000000)
            else:
                # draw rough rough times
                return (None, 0)
        # havent traversed enough need to start  
        else: # d
            return (None, score_position(board,RED_INT))

    # Finding best place for AI
    if maximizing_player:

        # worst score
        value = -math.inf

        # start out with whatever we are given
        column = random.choice(valid_locations)
        # each column we look down the tree and run the minimax on each depth level 
        for col in valid_locations:
            row = get_next_open_row(board, col)
            board_copy = board.copy()
            drop_chip(board_copy, row, col, RED_INT)
            #go one level deeper
            new_score = minimax(board_copy, depth-1, alpha, beta, False)[1]
            # if this score is better than what we have found so far
            if new_score > value:
                value = new_score
                column = col
            #alpha is the best score
            alpha = max(value, alpha) 
            # if our best move is better than the oppennent best move, we can remove it as the opponent will never take that direction
            if alpha >= beta:
                break

        return column, value
    
    # same as above, but for the minimizing player, with beta here serving as the inverse of what we were doing with alpha earlier, 
    # meaning we are pruning in the same way but from the vantage point of the current beta
    else: # for thte minimizing player
        value = math.inf
        column = random.choice(valid_locations)
        for col in valid_locations:
            row = get_next_open_row(board, col)
            board_copy = board.copy()
            drop_chip(board_copy, row, col, BLUE_INT)
            new_score = minimax(board_copy, depth-1, alpha, beta, True)[1]
            if new_score < value:
                value = new_score
                column = col
            beta = min(value, beta) 
            if alpha >= beta:
                break
        return column, value
def agent1move(board):
    # get the ai column
    col, minimax_score = minimax(board, 5, -math.inf, math.inf, True)
    return col
    
def agent2move(board,randomnum):
    # get a random column or a random column
    col = 0
    if randomnum == 1:
        valid_locations = get_valid_locations(board)
        col = random.choice(valid_locations)
        return col  
    else:
        col = int(input("Blue please choose a column(1-7): "))
        while col > 7 or col < 1:
            col = int(input("Invalid column, pick a valid one: "))
        while not is_valid_location(board, col - 1):
            col = int(input("Column is full. pick another one..."))
        col -= 1
        return col
# # # # # # # # # # # # # # main execution of the game # # # # # # # # # # # # # #
turn = 0
board = create_board()
print_board(board)
game_over = False
randomask = int(input("Do you want to play as random or make your own moves, type 1 for random 2 for input: "))
while not game_over:
    if turn % 2 == 0:
        col = agent1move(board)
        row = get_next_open_row(board, col)
        drop_chip(board, row, col, RED_INT)

    if turn % 2 == 1 and not game_over:
        col = agent2move(board,randomask)
        row = get_next_open_row(board, col)
        drop_chip(board, row, col, BLUE_INT)
        
        

    print_board(board)
    
    if game_is_won(board, RED_INT):
        game_over = True
        print(colored("Red wins!", 'red'))
    if game_is_won(board, BLUE_INT):
        game_over = True
        print(colored("Blue wins!", 'blue'))
    if len(get_valid_locations(board)) == 0:
        game_over = True
        print(colored("Draw!", 'blue'))
    turn += 1

#tmp = copy.deepcopy(board)