package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/*
 * aks8m - 5/20/18
 */
public class ChronologyReader extends TimedTaskWithProgressTracker<List<String>> implements PersistTaskResult {

    private final ReaderSpecification readerSpecification;
    private List<? extends Chronology> chronologiesToRead;

    public ChronologyReader(ReaderSpecification readerSpecification, List<? extends Chronology> chronologiesToRead) {
        this.readerSpecification = readerSpecification;
        this.chronologiesToRead = chronologiesToRead;



        updateTitle("Reading " + this.chronologiesToRead.size() + " " + this.readerSpecification.getReaderUIText());
        addToTotalWork(2);
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {

        final List<String> returnList = new ArrayList<>();

        try {
            completedUnitOfWork();

            this.chronologiesToRead.stream()
                    .forEach(chronology -> returnList.addAll(this.readerSpecification.readExportData(chronology)));

            this.readerSpecification.addColumnHeaders(returnList);

            completedUnitOfWork();

        }finally {
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
