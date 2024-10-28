import axios from "axios";

export default {
    startGame(names: string[]): Promise<any> {
        return axios.post("/api/start", {
            names: names
        });
    },

    getGame(gameKey: string): Promise<any> {
        return axios.get("/api/getgame", {
            params: {
                gameKey: gameKey
            }
        });
    },

    placeTile(pieceType: string, x: number, y: number): Promise<any> {
        return axios.get("/api/placetile", {
            params: {
                pieceType: pieceType,
                x: x,
                y: y
        }});
    },

    buyAndPlaceLlama(cropType: string, x: number, y:number): Promise<any> {
        return axios.get("/api/llama", {
            params: {
                cropType: cropType,
                x: x,
                y: y
        }});
    },

    endTurn(): Promise<any> {
        return axios.get("/api/endturn");
    },

    rotatePieces(): Promise<any> {
        return axios.get("/api/rotate");
    },

    flipPieces(): Promise<any> {
        return axios.get("/api/flip");
    }
}