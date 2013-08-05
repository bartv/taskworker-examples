/*
    Copyright 2013 KU Leuven Research and Development - iMinds - Distrinet

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Administrative Contact: dnet-project-office@cs.kuleuven.be
    Technical Contact: bart.vanbrabant@cs.kuleuven.be
*/

package drm.taskworker.workers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import drm.taskworker.Worker;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;

/**
 * Collect all files in a workflow and zip them when an end of workflow task
 * is received.
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class ZipWorker extends Worker {
	/**
	 * Creates a new work with the name blob-to-cache
	 */
	public ZipWorker(String workerName) {
		super(workerName);
	}

	/**
	 * Zip all files
	 */
	@SuppressWarnings("unchecked")
	public TaskResult work(Task task) {
		logger.info("Building zip file");
		TaskResult result = new TaskResult();
		if (!task.hasParam("arg0")) {
			return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
		}
		
		List<Object> data = (List<Object>) task.getParam("arg0");
		
		try {
			if (data.size() == 0) {
				logger.warning("empty zip file");
			}
			
			// create the zip stream
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			ZipOutputStream out = new ZipOutputStream(boas);

			// save the files in the zip
			int i = 0;
			for (Object entry : data) {
				out.putNextEntry(new ZipEntry(++i + ".pdf"));
				byte[] pdfData = (byte[])entry;
				out.write(pdfData);
			}
			out.close();
			boas.flush();
			
			byte[] zipData = boas.toByteArray();
			boas.close();
			
			Task newTask = new Task(task, this.getNextWorker(task.getJobId()));
			newTask.addParam("arg0", zipData);
			result.addNextTask(newTask);
		} catch (FileNotFoundException e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		} catch (IOException e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		}

		return result.setResult(TaskResult.Result.SUCCESS);
	}
}
