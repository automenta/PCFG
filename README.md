PCFG
====

Generates PCFG out of PennTreeBank sentences:
It reads the sample binarised PennTreeBank sentences from
the resource and loads them as trees and then calculates
the probabilities for each rule. Then calculates the CKY
chart and extracts the most probable parse tree for each
sentence using Viterbi (CKY) parsing.
