<template>
  <!-- If your source-code lives in a variable called 'sourcecode' -->
  <pre
    v-highlightjs="content"
  ><code class="java"></code></pre>
</template>

<script>
// Import Vue and vue-highlgihtjs
import Vue from 'vue'
import VueHighlightJS from 'vue-highlightjs'
import 'highlight.js/styles/atom-one-dark.css'
import {getFileContent} from '@/api/code/code'

// Tell Vue.js to use vue-highlightjs
Vue.use(VueHighlightJS)

export default {
  data() {
    return {
      content: '123',
      params: {
        localProjectPath: ''
      }
    }
  },
  methods: {
    readFileContent(param) {
      getFileContent(param).then(response => {
        console.log(response.msg);
        this.content = response.msg;
      })
    }
  },
  mounted() {
    let path = this.$route.query.localpath;
    // console.log(this.$route.query.localpath);
    this.params.localProjectPath = path;
    this.readFileContent(this.params);
  }
}

</script>
