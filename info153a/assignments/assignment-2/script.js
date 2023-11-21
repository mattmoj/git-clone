const quickFind = document.getElementById('quick-find');
const menuButton = document.getElementById('menu-button');
const navBar = document.getElementById('side-nav');
const taskList = document.getElementById('task-list');
const submitButton = document.getElementById('add-btn');
const cancelButton = document.getElementById('cancel-btn');
const addTask = document.getElementById('add-task');
const addInput = document.getElementById('add-input');
const taskInput = document.getElementById('task-text');

function displayItems() {
    const tasksFromStorage = getTasksFromStorage();
    tasksFromStorage.forEach((task) => addTaskToDOM(task));
  }
  
function onAddTaskSubmit(e) {
  e.preventDefault();

  const newTask = taskInput.value;

  if (newTask === '') {
    alert('Please add an item');
    return;
  }

  addTaskToDOM(newTask);
  addTaskToStorage(newTask);
  taskInput.value = '';

}

function toggleNav() {
    if (navBar.style.display === 'none') {
        navBar.style.display = 'flex';
    } else {
        navBar.style.display = 'none';

    }
}

function addTaskToDOM(task) {
    // Create list item
    const li = document.createElement('li');
    var input = document.createElement('input');
    input.setAttribute('type','checkbox');
    input.setAttribute('name','taskText');

    li.appendChild(input);
    // li.appendChild(label);
    li.appendChild(document.createTextNode(task));
  
    var hr = document.createElement('hr');
    hr.setAttribute('width','100%');

    // Add li to the DOM
    taskList.appendChild(li);
    taskList.appendChild(hr);
  }

function addTaskToStorage(task) {
    const tasksFromStorage = getTasksFromStorage();
  
    // Add new item to array
    tasksFromStorage.push(task);
  
    // Convert to JSON string and set to local storage
    localStorage.setItem('tasks', JSON.stringify(tasksFromStorage));
}
  
function getTasksFromStorage() {
    let tasksFromStorage;

    if (localStorage.getItem('tasks') === null) {
        tasksFromStorage = [];
    } else {
        tasksFromStorage = JSON.parse(localStorage.getItem('tasks'));
    }

    return tasksFromStorage;
}

function removeItemFromStorage(item) {
    let tasksFromStorage = getTasksFromStorage();
  
    // Filter out item to be removed
    tasksFromStorage = tasksFromStorage.filter((i) => i !== task);
  
    // Re-set to localstorage
    localStorage.setItem('tasks', JSON.stringify(tasksFromStorage));
  }

function filterItems(e) {
    const tasks = taskList.querySelectorAll('li');
    const text = e.target.value.toLowerCase();

    tasks.forEach((task) => {
        const taskName = task.firstChild.textContent.toLowerCase();

        if (taskName.indexOf(text) != -1) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

function toggleAdd() {
    if (addTask.style.display === 'none') {
        addTask.style.display = 'flex';
        addInput.style.display = 'none';
    } else {
        addTask.style.display = 'none';
        addInput.style.display = 'flex';
    }
}


// Initialize app
function init() {
    // Event Listeners
    menuButton.addEventListener('click', toggleNav);
    addTask.addEventListener('click', toggleAdd);
    cancelButton.addEventListener('click', toggleAdd);
    submitButton.addEventListener('click', onAddTaskSubmit);
    document.addEventListener('DOMContentLoaded', displayItems);
}

init();
  