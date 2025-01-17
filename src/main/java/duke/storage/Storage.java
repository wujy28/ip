package duke.storage;

import duke.tasks.DeadlineTask;
import duke.tasks.EventTask;
import duke.tasks.TaskList;
import duke.tasks.ToDoTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Encapsulates the storage system that handles the loading and saving of user data stored by the chatbot.
 * Handles the reading of data files and writing of saved data to hard drive to save the user's task list
 * across sessions.
 *
 * @author Wu Jingya
 */
public class Storage {
    /** The TaskList to be saved and loaded into */
    private TaskList taskList;
    /** The string specifying the path of the text file to store the TaskList data */
    private String path;

    /**
     * Constructs a Storage object using the specified TaskList object and data file path.
     * Initializes loading of data during object creation; in other words, this constructor
     * loads the TaskList data from the specified file during the construction of the object.
     *
     * @param taskList The TaskList to be saved and loaded.
     * @param path The file path as a string.
     * @see TaskList
     */
    public Storage(TaskList taskList, String path) {
        this.taskList = taskList;
        this.path = path;
        initialize();
    }

    //For testing purposes only
    /**
     * Constructs a Storage object using the specified TaskList object.
     * This constructor does not initialize the loading of data from the file into the TaskList
     * during object creation. This constructor is to be used for testing purposes only.
     *
     * @param taskList The TaskList to be saved and loaded.
     * @see TaskList
     */
    public Storage(TaskList taskList) {
        this.taskList = taskList;
        this.path = "";
    }

    private void initialize() {
        File savedData = new File(path);
        if (savedData.exists()) {
            loadTasksFromFile(savedData, taskList);
        }
    }

    /**
     * Loads the TaskList data recorded in the specified file into the target TaskList.
     * Shows an error message if the file cannot be found.
     *
     * @param file The File storing TaskList data.
     * @param taskList The TaskList object to be loaded into.
     * @see File
     * @see TaskList
     */
    public void loadTasksFromFile(File file, TaskList taskList) {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String taskData = scanner.nextLine();
                String[] dataSegments = taskData.split("[|]");
                switch (dataSegments.length) {
                case 3:
                    loadTodoTask(taskData);
                    break;
                case 4:
                    loadDeadlineTask(taskData);
                    break;
                case 5:
                    loadEventTask(taskData);
                    break;
                default:
                    System.out.println("Task formatting error: " + taskData + " not loaded");
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error scanning file!");
        }
    }

    private void loadTodoTask(String taskData) {
        String[] dataSegments = taskData.split("[|]");
        if (!dataSegments[0].equals("T")) {
            System.out.println("Task formatting error: " + taskData + " not loaded");
            return;
        }

        String description = dataSegments[2];
        Boolean isDone = false;
        if (dataSegments[1].equals("1")) {
            isDone = true;
        } else if (!dataSegments[1].equals("0")) {
            System.out.println("Task formatting error: " + taskData + " not loaded");
            return;
        }

        ToDoTask newToDo = new ToDoTask(description, isDone);
        assert taskList != null : "TaskList is null!";
        taskList.addTask(newToDo);
    }

    private void loadDeadlineTask(String taskData) {
        String[] dataSegments = taskData.split("[|]");
        if (!dataSegments[0].equals("D")) {
            System.out.println("Task formatting error: " + taskData + " not loaded");
            return;
        }

        String description = dataSegments[2];
        Boolean isDone = false;
        LocalDateTime by = LocalDateTime.parse(dataSegments[3]);
        if (dataSegments[1].equals("1")) {
            isDone = true;
        } else if (!dataSegments[1].equals("0")) {
            System.out.println("Task formatting error: " + taskData + " not loaded");
            return;
        }

        DeadlineTask newDeadline = new DeadlineTask(description, by, isDone);
        assert taskList != null : "TaskList is null!";
        taskList.addTask(newDeadline);
    }

    private void loadEventTask(String taskData) {
        String[] dataSegments = taskData.split("[|]");
        if (!dataSegments[0].equals("E")) {
            System.out.println("Task formatting error: " + taskData + " not loaded");
            return;
        }

        String description = dataSegments[2];
        Boolean isDone = false;
        LocalDateTime from = LocalDateTime.parse(dataSegments[3]);
        LocalDateTime to = LocalDateTime.parse(dataSegments[4]);
        if (dataSegments[1].equals("1")) {
            isDone = true;
        } else if (!dataSegments[1].equals("0")) {
            System.out.println("Task formatting error: " + taskData + " not loaded");
            return;
        }

        EventTask newEvent = new EventTask(description, from, to, isDone);
        assert taskList != null : "TaskList is null!";
        taskList.addTask(newEvent);
    }

    /**
     * Saves the TaskList stored by the Storage object into the text file specified by the path field.
     * Shows an error message if an error occurs when saving, and data will, as a result, not be saved.
     */
    public void saveData() {
        /* Tasks are saved in the following format:
        {TaskType: T/D/E} | {Done: 0/1} | Description | from/by date | to date */
        File dataFolder = new File("./data");
        if (!dataFolder.exists()) {
            if (dataFolder.mkdir()) {
                // Do nothing
            } else {
                System.out.println("Error creating data folder... Tasks not saved.");
            }
        }

        File savedData = new File(path);
        try {
            FileWriter writer = new FileWriter(savedData, false);
            savedData.createNewFile();
            writer.write(taskList.getTaskListData());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving data... Tasks not saved.");
        }
    }
}
