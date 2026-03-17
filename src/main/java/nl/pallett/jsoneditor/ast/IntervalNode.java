package nl.pallett.jsoneditor.ast;

import java.util.ArrayList;
import java.util.List;

class IntervalNode {

    int center;

    List<AstNode> overlapping = new ArrayList<>();

    IntervalNode left;
    IntervalNode right;
}
