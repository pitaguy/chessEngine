package chessEngine;

import java.util.HashMap;

public interface ComputerPlayer {
	
	Move getCpuMove(Board curBoard,HashMap<Board,Integer> countVisited);

}
