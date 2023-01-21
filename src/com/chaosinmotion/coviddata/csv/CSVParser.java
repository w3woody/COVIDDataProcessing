package com.chaosinmotion.coviddata.csv;

/**
 * The CSVParser class provides a way to parse the data stored in a CSV table.
 * This assumes--and it appears to be consistent with the data downloaded--that
 * the first row would be the column headers. The first row is then loaded into
 * a map allowing us to quickly go from 'row header' to index.
 *
 * The subsequent rows are loaded into memory--which all implies you need a
 * fairly large amount of memory to parse all of this as the largest of the
 * files loaded is 25GB.
 */
public class CSVParser
{
}
