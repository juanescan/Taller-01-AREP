function addTask() {
    let name = document.getElementById("taskName").value;
    let time = document.getElementById("taskTime").value;
    fetch(`/tasks?name=${encodeURIComponent(name)}&time=${encodeURIComponent(time)}`, {method: 'POST'})
            .then(r => r.text()).then(t => {
        alert(t);
        listTasks();
    });
}

function listTasks() {
    fetch("/tasks")
            .then(r => r.json())
            .then(data => {
                let list = document.getElementById("tasksList");
                list.innerHTML = "";
                data.forEach(task => {
                    let li = document.createElement("li");
                    li.textContent = `${task.name} - ${task.time}`;
                    li.onclick = () => deleteTask(task.name, task.time);
                    list.appendChild(li);
                });
            });
}

function deleteTask(name, time) {
    fetch(`/tasks?name=${encodeURIComponent(name)}&time=${encodeURIComponent(time)}`, {method: 'DELETE'})
            .then(r => r.text()).then(t => {
        alert(t);
        listTasks();
    });
}
