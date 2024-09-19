extends Sprite2D
class_name CheckersBoardTop

#var replay = "board:0,2,0,2,0,2,0,2,2,0,2,0,2,0,2,0,0,2,0,2,0,2,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0|move:5,2,4,3|board:0,2,0,2,0,2,0,2,2,0,2,0,2,0,2,0,0,2,0,2,0,0,0,2,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0";
var black_king_texture = preload("res://checker_black_king.png")
var red_king_rexture = preload("res://checker_red_king.png")

var replay = null;
var redPiece: Sprite2D

var boardHighlight: BoardHighlight
var highlights: Array[BoardHighlight]

var clicked_piece: Sprite2D
var clickedPiecePos: Vector2

var moves: Array[Vector2]
var has_moved: bool = false
var prev_move: Array[Vector2]

var has_connected = false

var player = -1

# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	var appPlugin := Engine.get_singleton("AppPlugin")
	if appPlugin:
		print("App plugin is available")
		if not has_connected:
			appPlugin.connect("set_replay", _set_replay)
			has_connected = true
	else:
		print("App plugin is not available")
	
	if replay == null:
		return
		
	player = appPlugin.getPlayer()
	print("got the player: ", player)
		
	redPiece = get_node("CheckerPieceRed")
	var blackPiece: Sprite2D = get_node("CheckerPieceBlack")
	boardHighlight = get_node("BoardHighlight")
	
	var prevBoard = replay.split('|')[0].substr(6).split(',');
	
	var replayMoves = Array()
	
	for elem in replay.split('|'):
		var spl = elem.split(':')
		if spl[0] == 'move' || spl[0] == 'attack':
			replayMoves.append(elem)
	
	var nextBoard = replay.split('|')[2].substr(6).split(',');
	
	print(replayMoves)
	
	for y in range(0, 8):
		for x in range(0, 8):
			var val = prevBoard[(7 - y) * 8 + x];
			var newPiece: Sprite2D = null;
			if val == "2":
				newPiece = blackPiece.duplicate()
			elif val == "1":
				newPiece = redPiece.duplicate();
				
			if newPiece != null:
				newPiece.position = Vector2(redPiece.position.x + (135 * x), redPiece.position.y + (135 * y));
				newPiece.name = str(x) + "," + str(7-y)
				newPiece.visible = true
				add_child(newPiece)
				print(newPiece.name, " : ", newPiece.position)
				
				if replayMoves.size() > 0:
					var tween = newPiece.get_tree().create_tween()
					for move in replayMoves:
						var moveType = move.split(':')[0]
						var movePos = move.split(':')[1].split(',')
						if newPiece.name == movePos[0] + ',' + movePos[1]:
							var newPos = Vector2(redPiece.position.x + (135 * int(movePos[2])), redPiece.position.y + (135 * (7 - int(movePos[3]))))
							tween.tween_property(newPiece, "position", newPos, 1.0).set_trans(Tween.TRANS_SINE)
							newPiece.name = movePos[2] + "," + movePos[3];
							if moveType == "attack":
								jump_piece(int(movePos[0]), int(movePos[1]), int(movePos[2]), int(movePos[3]))

func _set_replay(new_replay: String):
	replay = new_replay
	_ready()

func export_replay() -> String:
	var board: Array[String]
	for i in range(0, 64):
		board.append("0")
	
	for y in range(0, 8):
		for x in range(0, 8):
			var piece: Sprite2D = get_node_or_null(str(x) + "," + str(7-y))
			if piece != null:
				var color = get_piece_color(piece)
				if color == "red":
					board[(7 - y) * 8 + x] = "1"
				elif color == "black":
					board[(7 - y) * 8 + x] = "2"
	
	var boardStr: String
	for val in board:
		boardStr += val + ","
		
	var moveType = "move"
	if abs(prev_move[0].x - prev_move[1].x) > 1:
		moveType = "attack"
	
	if player == 1:
		player = 2
	elif player == 2:
		player = 1
	
	return replay.split('|')[2] + "|move:" + str(prev_move[0].x) + "," + str(7 - prev_move[0].y) + "," + str(prev_move[1].x) + "," + str(prev_move[1].y) + "|board:" + boardStr.substr(0, boardStr.length()-1)
	
func jump_piece(prevX: int, prevY: int, newX: int, newY: int):
	var x_step = 1 if newX > prevX else -1
	var y_step = 1 if newY > prevY else -1
	var jumpedPiece: Sprite2D = get_node_or_null(str(prevX + x_step) + "," + str(prevY + y_step))
	if jumpedPiece != null:
		var tween = jumpedPiece.get_tree().create_tween()
		var modulate_color = jumpedPiece.self_modulate
		modulate_color.a = 0.0
		tween.tween_property(jumpedPiece, "self_modulate", modulate_color, 1.0).set_trans(Tween.TRANS_LINEAR)
		jumpedPiece.name = "jumped"

func move_piece(piece: Sprite2D, x: int, y: int):
	var newPos = Vector2(redPiece.position.x + (135 * x), redPiece.position.y + (135 * y))
	var tween = piece.get_tree().create_tween()
	tween.tween_property(piece, "position", newPos, 0.5).set_trans(Tween.TRANS_SINE)
	var color = get_piece_color(piece)
	if (color == "black" and (7-y) == 7) or (color == "red" and (7-y) == 0):
		tween.tween_callback(set_checker_king.bind(piece, color))
	piece.name = str(x) + "," + str(7-y)

func set_checker_king(piece: Sprite2D, color: String):
	if color == "red":
		piece.texture = red_king_rexture
	elif color == "black":
		piece.texture = black_king_texture
		
func is_checker_king(piece: Sprite2D) -> bool:
	if piece.texture.resource_path.contains("king"):
		return true
	return false

func add_highlight(x: int, y: int):
	var newHighlight: BoardHighlight = boardHighlight.duplicate()
	highlights.append(newHighlight)
	add_child(newHighlight)
	var basePos: Vector2 = boardHighlight.get_meta("Position")
	var newPos = Vector2(basePos.x + (81 * x), basePos.y + (81 * y))
	newHighlight.set_meta("Position", newPos)
	newHighlight.set_meta("Visible", true);
	print("Highlighting: ", x, ",", y);


func clear_highlights():
	for highlight in highlights:
		highlight.free()
	highlights.clear()

func get_piece_color(piece: Sprite2D) -> String:
	if piece.texture.resource_path.contains("red"):
		return "red"
	elif piece.texture.resource_path.contains("black"):
		return "black"
	return "unknown"

func gen_moves():
	moves.clear()
	var diagonals: Array[Vector2]
	var color = get_piece_color(clicked_piece)
	var isKing = is_checker_king(clicked_piece)
	if color == "black" or isKing:
		diagonals.append(Vector2(-1, 1))
		diagonals.append(Vector2(1, 1))
	elif color == "red" or isKing:
		diagonals.append(Vector2(1, -1));
		diagonals.append(Vector2(-1, -1));
		
	for diagonal in diagonals:
		var pos = Vector2(clickedPiecePos.x + diagonal.x, clickedPiecePos.y + diagonal.y)
		var piece = get_node_or_null(str(pos.x) + "," + str(pos.y))
		if piece == null:
			moves.append(pos)
			add_highlight(pos.x, 7 - pos.y)
		elif !check_player(piece): #check for jumps
			var x_step = 1 if pos.x > clickedPiecePos.x else -1
			var y_step = 1 if pos.y > clickedPiecePos.y else -1
			var newPos = Vector2(pos.x + x_step, pos.y + y_step)
			if get_node_or_null(str(newPos.x) + "," + str(newPos.y)) == null:
				moves.append(newPos)
				add_highlight(newPos.x, 7 - newPos.y)


func undo_move():
	var piece: Sprite2D = get_node(str(prev_move[1].x) + "," + str(prev_move[1].y))
	move_piece(piece, prev_move[0].x, abs(prev_move[0].y - 7))
	has_moved = false
	(get_node("../UndoButton") as Button).disabled = true
	(get_node("../SendButton") as Button).disabled = true

func check_player(piece: Sprite2D) -> bool:
	var color = get_piece_color(piece)
	if player == 1 and color == "black":
		return true
	elif player == 2 and color == "red":
		return true
	return false

func _input(event: InputEvent) -> void:
	if event is InputEventMouseButton:
		if event.pressed and event.button_index == 1 and not has_moved:
			var x: int = ceil(event.position.x / 80) - 1;
			var y: int = ceil(event.position.y / 80) - 4;
			print("board position at ", x, ",", 7-y, " clicked")
			
			var clickedPiece: Sprite2D = get_node_or_null(str(x) + "," + str(7-y))
			if clickedPiece != null:
				if check_player(clickedPiece):
					clear_highlights()
					clicked_piece = clickedPiece
					clickedPiecePos = Vector2(x, 7-y)
					add_highlight(x, y)
					gen_moves()
			elif clicked_piece != null and moves.has(Vector2(x, 7-y)):
				move_piece(clicked_piece, x, y)
				clicked_piece = null
				clear_highlights()
				has_moved = true
				prev_move.insert(0, clickedPiecePos)
				prev_move.insert(1, Vector2(x, 7-y))
				if abs(prev_move[0].x - prev_move[1].x) > 1:
					jump_piece(prev_move[0].x, prev_move[0].y, prev_move[1].x, prev_move[1].y)
				(get_node("../UndoButton") as Button).disabled = false
				(get_node("../SendButton") as Button).disabled = false
				
# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta: float) -> void:
	pass
