extends Sprite2D
class_name CheckersBoardTop

#var replay = "board:0,2,0,2,0,2,0,2,2,0,2,0,2,0,2,0,0,2,0,2,0,2,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0|move:5,2,4,3|board:0,2,0,2,0,2,0,2,2,0,2,0,2,0,2,0,0,2,0,2,0,0,0,2,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0";
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
		
	redPiece = get_node("CheckerPieceRed")
	var blackPiece: Sprite2D = get_node("CheckerPieceBlack")
	boardHighlight = get_node("BoardHighlight")
	
	var prevBoard = replay.split('|')[0].substr(6).split(',');
	var move = replay.split('|')[1].substr(5).split(',');
	var nextBoard = replay.split('|')[2].substr(6).split(',');
	
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
				if newPiece.name == move[0] + ',' + move[1]:
					var newPos = Vector2(redPiece.position.x + (135 * int(move[2])), redPiece.position.y + (135 * (int(move[3])+1)))
					var tween = newPiece.get_tree().create_tween()
					tween.tween_property(newPiece, "position", newPos, 1.0).set_trans(Tween.TRANS_SINE)
					newPiece.name = move[2] + "," + move[3];

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
				if piece.texture.resource_path.contains("red"):
					board[(7 - y) * 8 + x] = "1"
				elif piece.texture.resource_path.contains("black"):
					board[(7 - y) * 8 + x] = "2"
	
	var boardStr: String
	for val in board:
		boardStr += val + ","
	
	return replay.split('|')[2] + "|move:" + str(prev_move[0].x) + "," + str(prev_move[0].y) + "," + str(prev_move[1].x) + "," + str(prev_move[1].y) + "|board:" + boardStr.substr(0, boardStr.length()-1)


func move_piece(piece: Sprite2D, x: int, y: int):
	var newPos = Vector2(redPiece.position.x + (135 * x), redPiece.position.y + (135 * y))
	var tween = piece.get_tree().create_tween()
	tween.tween_property(piece, "position", newPos, 0.5).set_trans(Tween.TRANS_SINE)
	piece.name = str(x) + "," + str(7-y)

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


func gen_moves():
	moves.clear()
	var diagonals: Array[Vector2]
	var name = clicked_piece.texture.resource_path
	if name.contains("black"):
		diagonals.append(Vector2(-1, 1))
		diagonals.append(Vector2(1, 1))
	elif name.contains("red"):
		diagonals.append(Vector2(1, -1));
		diagonals.append(Vector2(-1, -1));
		
	for diagonal in diagonals:
		var pos = Vector2(clickedPiecePos.x + diagonal.x, clickedPiecePos.y + diagonal.y)
		if get_node_or_null(str(pos.x) + "," + str(pos.y)) == null:
			moves.append(pos)
			add_highlight(pos.x, 7 - pos.y)


func undo_move():
	var piece: Sprite2D = get_node(str(prev_move[1].x) + "," + str(prev_move[1].y))
	move_piece(piece, prev_move[0].x, abs(prev_move[0].y - 7))
	has_moved = false
	(get_node("../UndoButton") as Button).disabled = true
	(get_node("../SendButton") as Button).disabled = true


func _input(event: InputEvent) -> void:
	if event is InputEventMouseButton:
		if event.pressed and event.button_index == 1 and not has_moved:
			var x: int = ceil(event.position.x / 80) - 1;
			var y: int = ceil(event.position.y / 80) - 4;
			print("board position at ", x, ",", 7-y, " clicked")
			
			var clickedPiece: Sprite2D = get_node_or_null(str(x) + "," + str(7-y))
			if clickedPiece != null:
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
				(get_node("../UndoButton") as Button).disabled = false
				(get_node("../SendButton") as Button).disabled = false
				
# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta: float) -> void:
	pass
