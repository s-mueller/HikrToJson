package extractdata.extractors;

import extractdata.pojo.Extractable;

import java.util.List;

public interface Extractor {

    List<? extends Extractable> extract();

    String createTableStatement();
}
