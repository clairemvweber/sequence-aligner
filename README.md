# Pairwise Sequence Alignment
---
This program performs two types of pairwise alignment of the *query* sequence and the *reference* sequence. The program will output the optimal score and the **CIGAR** string representation of the edits that must be made to transform the reference sequence into the query sequence. 

The first type of alignment is a **global** alignment that implements the Needleman-Wunsch algorithm with linear gap costs. 

The second type of alignment is a semi-global or **fitting** alignment that that implements a hybrid of the Needleman-Wunsch and Smith-Waterman algorithms also with linear gap costs. The computation for fitting alignment will be the same as the global alignment, except it will also determine where on the reference the optimal alignment starts and where on the reference the optimal alignment ends. As such, the CIGAR string representation will not report and leading or trailing gaps and will instead report the positions in the reference that are aligned to the query.



### Input
The seq-aligner program will take the following arguments:
- `input_file` - the path to an input file of sequence pairs in the format specified below
- `method` - the type of alignments to be computed (must be the strings `global` or `fitting`)
- `mismatch_penalty` - a positive integer `m`, such that the score of a mismatch in an alignment will be `-m`
- `gap_penalty` - a positive integer `g`, such that the score of each gap character inserted into the alignment will be `-g`
- `output_file` - the path to the file where the output will be written

**Problem Records** will have the format specified below:
```
problem_name
query
reference
```

### Output
The seq-aligner program will output the following in addition to the Problem Record:
- `score` - this is the optimal score computed by the chosen alignment method\
- `ref_start` - this is the location on the reference sequence where the alignment begins (for global alignments this is always 0)
- `ref_end` - this is the location on the reference sequence where the alignment ends (for global alignments this is always the length of the reference sequence)
- `CIGAR` - this is the extended CIGAR string

```
problem_name
query
reference
score ref_start ref_end CIGAR 
```
