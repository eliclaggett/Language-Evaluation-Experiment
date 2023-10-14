
async function init () {
  await Promise.all([
    Breadboard.loadConfig(),
    Breadboard.connect(),
    Breadboard.addScriptFromURL('/generated/breadboard-v2.4/js/chunk-vendors.js'),
  	Breadboard.addScriptFromURL('/generated/breadboard-v2.4/js/app.js'),
    Breadboard.addStyleFromURL('/generated/breadboard-v2.4/css/chunk-vendors.css'),
    Breadboard.addStyleFromURL('/generated/breadboard-v2.4/css/app.css'),
    Breadboard.addScriptFromURL('https://www.google.com/recaptcha/api.js'),
    
    // some step 1 imports
    Breadboard.addScriptFromURL('https://cdn.jsdelivr.net/npm/sortablejs@1.8.4/Sortable.min.js'),
    Breadboard.addScriptFromURL('https://cdnjs.cloudflare.com/ajax/libs/Vue.Draggable/2.20.0/vuedraggable.umd.min.js')
  ])
}

init()