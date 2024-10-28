<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{
  (e: 'names', names: string[]): void
}>()

let names = ref([""])

function startGame(){
    if (names.value[0] && names.value[1]) {
        emit('names', names.value)
    }
}
</script>

<template>
<div class="sign-in flex items-center justify-center bg-orange-50 flex-col">
    <span class="font-bold text-orange-700 text-3xl mb-8">What are the players' names?</span>
    <input v-model="names[0]" placeholder="Player 1" class="mb-3 p-2 text-xl">
    <input v-model="names[1]" placeholder="Player 2" class="mb-12 p-2 text-xl">
    <button :disabled="!names[0] && !names[1]" 
                class="start-button text-white font-bold text-3xl mt-12"
                @click="startGame()">Start</button>
</div>
</template>

<style scoped>
.sign-in {
    height: calc(100vh - 80px);
}

.start-button {
    padding: 10px 50px;
    background-color: rgb(234 88 12);
}

.start-button:disabled {
    background-color: rgb(148, 48, 9);
}

.start-button:disabled:hover {
    background-color: rgb(148, 48, 9);
}

.start-button:hover {
    background-color: rgb(207, 80, 11);
}

</style>
