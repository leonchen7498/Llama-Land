<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Board from './components/Board.vue'
import SignIn from './components/SignIn.vue';
import { useCookies } from "vue3-cookies";
const { cookies } = useCookies();

let names = ref([""])
let loggedIn = ref(false);

onMounted(async () => {
  if (cookies.get("gameKey")) {
    loggedIn.value = true;
  }
});

function setNames(newNames: string[]) {
  names.value = newNames;
  loggedIn.value = true;
}

function resetGame(){
  cookies.remove("gameKey")
  location.reload();
}

</script>

<template>
  <div class="flex w-screen items-center justify-center bg-orange-600">
    <button v-if="loggedIn" @click="resetGame()" 
    class="reset-button absolute p-4 top-2.5 left-3 bg-orange-700 text-white font-extrabold text-xl">Reset game</button>
    <img src="/llama.svg" class="logo" alt="Llama" />
    <span class="logo-text text-neutral-100 font-extrabold">Llama Land</span>
  </div>
  <SignIn v-if="!loggedIn" @names="setNames"/>
  <Board v-if="loggedIn" :names="names"/>
</template>

<style scoped>
.flex {
  display:flex;
}
.logo {
  height: 4em;
}

.logo-text {
  font-size: 40px;
  line-height: 2em;
  margin-left: 20px;
}
</style>
