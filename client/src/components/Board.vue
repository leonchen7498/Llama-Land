<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as service from '../services/ApiService'
import { useCookies } from "vue3-cookies";
const { cookies } = useCookies();

const props = defineProps<{
  names: string[]
}>()

let tiles = ref([[]]);
let inventory = ref([]);
let queue = ref([])
let name = ref("")
let hasPlacedPiece = ref(false)
let gridHoverX = ref(0);
let gridHoverY = ref(0);

let selectedPiece = ref(null);
let selectedCrop = ref("");
let showWarningText = ref("")
let showWarning = ref(false)

onMounted(async () => {
    let gameKey = cookies.get("gameKey");
    if (gameKey) {
        service.default.getGame(gameKey).then(response =>{
            tiles.value = response.data.boards[0].tiles;
            inventory.value = response.data.boards[0].inventory;
            queue.value = response.data.placeablePieces;
            hasPlacedPiece.value = response.data.boards[0].hasPlacedPiece;
            name.value = response.data.boards[0].name;
        });
    }
    else {
        service.default.startGame(props.names).then(response => {
        tiles.value = response.data.boards[0].tiles;
        inventory.value = response.data.boards[0].inventory;
        queue.value = response.data.placeablePieces;
        hasPlacedPiece.value = response.data.boards[0].hasPlacedPiece;
        name.value = response.data.boards[0].name;

        cookies.set("gameKey", response.data.gameKey);
    })}
});

function resetValues(response: any){
    tiles.value = response.data.boards[0].tiles;
    inventory.value = response.data.boards[0].inventory;
    queue.value = response.data.placeablePieces;
    hasPlacedPiece.value = response.data.boards[0].hasPlacedPiece;
    name.value = response.data.boards[0].name;
    selectedPiece.value = null;
    selectedCrop.value = "";
    showWarning.value = false;
}

function pieceClicked(piece: any) {
    if (!hasPlacedPiece.value) {
        if (selectedPiece.value == piece){
            selectedPiece.value = null
        }
        else {
            selectedPiece.value = piece;
            selectedCrop.value = "";
            gridHoverX.value = -99;
            gridHoverY.value = -99;
        }
    }
}

function gridClicked(x: number, y: number){
    if (selectedPiece.value) {
        service.default.placeTile(selectedPiece.value.pieceType, x, y).then(response => {
            resetValues(response);
        }).catch(() => {
            showWarning.value = true
            showWarningText.value = "That is an invalid move!"
        })
    }
    else if (selectedCrop.value) {
        service.default.buyAndPlaceLlama(selectedCrop.value, x, y).then(response => {
            resetValues(response);
        }).catch(() => {
            showWarning.value = true
            showWarningText.value = "You can't place a llama here!"
        })
    }
    else {
        showWarning.value = true
        showWarningText.value = "Please select something first!"
    }
}

function buyLlamaClicked(cropType: string) {
    if (cropType==selectedCrop.value) {
        selectedCrop.value = "";
    }
    else {
        selectedPiece.value = null;
        selectedCrop.value = cropType;
    }
} 

function endTurnClicked() {
    if (hasPlacedPiece.value) {
        service.default.endTurn().then(response => {
            resetValues(response);
        });
    }
}

function rotateButtonClicked() {
    if (!hasPlacedPiece.value) {
        service.default.rotatePieces().then(response => {
                resetValues(response);
        });
    }
}

function flipButtonClicked() {
    if (!hasPlacedPiece.value) {
        service.default.flipPieces().then(response => {
                resetValues(response);
        });
    }
}

function onGridHover(x: number, y: number) {
    if (x > 0 && y > 0 && x < 15 && y < 15) {
        gridHoverX.value = x-1;
        gridHoverY.value = y-1;
    }
}

</script>

<template>
    <span v-if="!name" class="loading text-5xl">🦙</span>
    <div class="board flex items-center justify-center bg-orange-50">
        <div v-if="selectedPiece" class="board-grid grid grid-cols-16 content-center z-10 absolute">
            <template v-for="(, row) in tiles.length">
                <template v-for="(, col) in tiles[row].length">
                    <div v-if="row >= gridHoverY && row <= gridHoverY + 2 && 
                    col >= gridHoverX && col <= gridHoverX + 2" 
                    @mouseenter="onGridHover(col, row)" 
                    @click="gridClicked(col, row)"
                    :class="['tile', {
                        'bg-lime-400 opacity-80': selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Grass',
                        'bg-amber-200 opacity-80': selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Corn',
                        'bg-amber-700 opacity-80': selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Potato',
                        'bg-lime-900 opacity-80': selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Cocoa',
                        'opacity-0 border-black border': selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'None'}]">
                        <div v-if="selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Corn'" class="emoji">🌽</div>
                        <div v-if="selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Potato'" class="emoji">🥔</div>
                        <img v-if="selectedPiece.pieceTiles[row - gridHoverY][col - gridHoverX].tileType == 'Cocoa'" src="../assets/cocoa-bean.png" alt="Cocoa bean"/>
                    </div>
                    <div v-else class="tile opacity-0" @mouseenter="onGridHover(col, row)" ></div>
                </template>
            </template>
        </div>
        <div v-if="name" class="board-grid grid grid-cols-16 content-center z-0">
            <template v-for="(, row) in tiles.length">
                <template v-for="(, col) in tiles[row].length">
                    <div @click="gridClicked(col, row)" 
                    :class="['tile ', {
                        'bg-lime-400': tiles[row][col].tileType == 'Grass',
                        'bg-amber-200': tiles[row][col].tileType == 'Corn',
                        'bg-amber-700': tiles[row][col].tileType == 'Potato',
                        'bg-lime-900': tiles[row][col].tileType == 'Cocoa',
                        'opacity-40 bg-stone-100 border-black border-opacity-5 border': tiles[row][col].tileType == 'None',
                        'border-black border-opacity-20 border border-dashed': tiles[row][col].height == 1,
                        'border-red-600 border-opacity-80 border-2': tiles[row][col].height == 2,
                        'border-green-500 border-opacity-100 border-2': tiles[row][col].height == 3,
                        'border-purple-600 border-opacity-100 border-2': tiles[row][col].height >= 4
                        }]">
                        <div v-if="tiles[row][col].tileType == 'Corn'" class="emoji">🌽</div>
                        <div v-if="tiles[row][col].tileType == 'Potato'" class="emoji">🥔</div>
                        <img v-if="tiles[row][col].tileType == 'Cocoa'" src="../assets/cocoa-bean.png" alt="Cocoa bean"/>
                        <div v-if="tiles[row][col].hasLlama == true" class="llama">🦙</div>
                        <span v-if="tiles[row][col].tileType != 'None'" class="text-white font-bold">{{ tiles[row][col].height }}</span>
                    </div>
                </template>
            </template>
        </div>
        <div v-if="name" class="queue absolute right-0">
            <button :disabled="hasPlacedPiece == false" 
                class="end-turn-button relative text-white font-bold text-2xl mt-12"
                @click="endTurnClicked()">End turn</button>

            <span class="font-bold text-orange-700">Choose a piece and place it on the board</span>
            <template v-for="piece in queue">
                <div @click="pieceClicked(piece)" :class="['grid grid-cols-3', {
                    'piece-grid-selected': selectedPiece == piece,
                    'piece-grid': !hasPlacedPiece,
                    'piece-grid-disabled': hasPlacedPiece
                }]">
                    <template v-for="(, row) in piece.pieceTiles.length">
                        <template v-for="(, col) in piece.pieceTiles[row].length">
                            <div :class="['tile border border-black border-opacity-10', {
                                'bg-lime-400': piece.pieceTiles[row][col].tileType == 'Grass',
                                'bg-amber-200': piece.pieceTiles[row][col].tileType == 'Corn',
                                'bg-amber-900': piece.pieceTiles[row][col].tileType == 'Potato',
                                'bg-lime-900': piece.pieceTiles[row][col].tileType == 'Cocoa',
                                'opacity-40 bg-stone-100': piece.pieceTiles[row][col].tileType == 'None'}]">
                                    <div v-if="piece.pieceTiles[row][col].tileType == 'Corn'" class="emoji">🌽</div>
                                    <div v-if="piece.pieceTiles[row][col].tileType == 'Potato'" class="emoji">🥔</div>
                                    <img v-if="piece.pieceTiles[row][col].tileType == 'Cocoa'" src="../assets/cocoa-bean.png" alt="Cocoa bean"/>
                            </div>
                        </template>
                    </template>
                </div>
            </template>
            <button :disabled="hasPlacedPiece == true" class="rotate font-bold" 
                @click="rotateButtonClicked">Rotate</button>
                <button :disabled="hasPlacedPiece == true" class="rotate font-bold" 
                    @click="flipButtonClicked">Flip</button>
        </div>
        <div v-if="name" class="inventory absolute left-0">
            <div class="font-bold text-orange-700 mt-12">Player</div>
            <div class="font-bold text-4xl name">{{ name }}</div>
            <span v-if="selectedCrop==''" class="font-bold text-orange-700">Inventory</span>
            <span v-else class="font-bold text-orange-700">Where would you like to place the llama?</span>
            <template v-for="item in inventory">
                <div class="inventory-item">
                    <span v-if="item.cropType == 'Corn'" class="emoji">🌽</span>
                    <span v-if="item.cropType == 'Potato'" class="emoji">🥔</span>
                    <img v-if="item.cropType == 'Cocoa'" src="../assets/cocoa-bean.png" alt="Cocoa bean"/>
                    <span class="ml-2">{{item.amount}}</span>
                    <button class="buy" :disabled="item.amount < 4" @click="buyLlamaClicked(item.cropType)">
                        <span v-if="selectedCrop!=item.cropType">Buy llama</span>
                        <span v-else>Cancel</span>
                    </button>
                </div>
            </template>
        </div>
        <div v-if="showWarning == true" class="z-20 warning bg-red-100 border border-red-400 text-red-700 rounded absolute bottom-3" role="alert">
            <strong class="font-bold">{{ showWarningText }}</strong>
            <span @click="showWarning=false" class="absolute top-0 bottom-0 right-0">
                <svg class="fill-current h-6 w-6 text-red-500" role="button" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><title>Close</title><path d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z"/></svg>
            </span>
        </div>
    </div>
</template>

<style scoped>
.warning {
    padding: 15px 25px 15px 15px
}

.board {
  color: #888;
  height: calc(100vh - 80px);
  --main-color: rgb(234 88 12);
}

.board-grid {
    width: 1000px;
    border: dashed var(--main-color);
}

.inventory-item {
    border: dashed var(--main-color);
    width: 80%;
    margin: 20px;
    padding: 10px;
    display: block;
    justify-content: center;
    font-size: 22px;
    line-height: 45px;
    color: var(--main-color);
    font-weight: bold;
}

.inventory-item img {
    height: 30px;
    width: auto;
    display:inline;
    vertical-align: text-top;
}

.buy {
    background-color: var(--main-color);
    color: rgb(243, 243, 243);
    padding: 5px;
    width: 100%;
}

.rotate {
    background-color: var(--main-color);
    color: rgb(243, 243, 243);
    padding: 5px;
    width: 40%;
    margin: 0px 10px;
}

.buy:disabled, .end-turn-button:disabled, .rotate:disabled {
    background-color: rgb(148, 48, 9);
}

.buy:disabled:hover, .end-turn-button:disabled:hover, .rotate:disabled:hover {
    background-color: rgb(148, 48, 9);
}

.buy:hover, .end-turn-button:hover, .rotate:hover {
    background-color: rgb(207, 80, 11);
}

.end-turn-button {
    width: 80%;
    padding: 10px;
    margin-bottom: 60px;
    background-color: var(--main-color);
}

.queue, .inventory {
    width: 200px;
    display: block;
    text-align: center;
    height: calc(100vh - 80px);
}

.inventory {
    margin-left: 20px;
}

.name {
    margin-bottom: 100px;
    color: var(--main-color)
}

.queue {
    margin-right: 20px;
}

.piece-grid, .piece-grid-disabled {
    width: 80%;
    margin: 20px;
}

.piece-grid {
    border: dashed var(--main-color);
}

.piece-grid-disabled {
    border: dashed rgb(189, 101, 0);
}

.piece-grid:hover {
    border: dashed rgb(189, 101, 0);
    cursor: pointer;
}

.piece-grid-selected, .piece-grid-selected:hover {
    border: dashed rgb(124, 66, 0);
    cursor: pointer;
}

.tile {
    height: 50px;
    position: relative;
    display:flex;
    justify-content: center;
}

.tile img {
    margin-left: auto;
    margin-right: auto;
    height: 30px;
    margin-top: 8px;
    width: auto;
}

.emoji {
    font-size: 22px;
    line-height: 45px;
}

.llama {
    font-size: 28px;
    line-height: 45px;
}

.tile span {
    bottom: 0;
    right: 5px;
    position: absolute;
    -webkit-text-stroke: 1px black;
    -webkit-user-select: none; 
    user-select: none; 
}

.tile:hover {
    border: 2px solid black;
}

.loading {
    position: absolute; 
    left: 0; 
    right: 0; 
    z-index: 1;
    margin-inline: auto; 
    top: 50%;
    width: fit-content;
    animation: spin 1000ms linear 0s infinite; 
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  25% {
    transform: rotate(90deg);
  }

  50% {
    transform: rotate(180deg);
  }

  75% {
    transform: rotate(270deg);
  }

  100% {
    transform: rotate(360deg);
  }
}
</style>
