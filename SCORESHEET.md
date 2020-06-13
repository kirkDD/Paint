# Scoresheet for as6-undo
Commit SHA: 822ca2d3b689eac49f9d01d989bb91bc03d95919

**19 / 20 : Total Score**
- **2 / 2 : Part 0: Implement ChangeThicknessAction**
     - 1 / 1 : Correct implementation of doAction
     - 1 / 1 : Correct implementation of undoAction
- **7 / 7 : Part 1: StackHistory**
     - 1 / 1 : Implementation of addAction adds action to the stack
     - 1 / 1 : Implementation of addAction deletes everything in the redo stack
     - 1 / 1 : Correct implementation of undo
     - 1 / 1 : Correct implementation of redo
     - 1 / 1 : Correct implementation of clear
     - **2 / 2 : Correctly implements Capacity**
          - 1 / 1 : Default capacity respected
          - 1 / 1 : Larger and Smaller capacities respected
- **3 / 3 : Part 2: Adding thickness to FAB thickness menu**
     - 1 / 1 : New FAB appears in the list in the menu in ReversibleDrawingActivity
     - 1 / 1 : New FAB has content description (in strings.xml)
     - 1 / 1 : Can draw with new 0 thickness in ReversibleDrawingActivity
- **6.0 / 7 : Part 3: Integrating ColorPickerView**
     - **3.0 / 4 : ColorPicker open/close toggles disables/enables buttons**
          - 1 / 1 : Color wheel is visible/invisible
          - 0.5 / 1 : Toggle correct for undo
               - mUndoStack.addLast is not using deque as a stack. Instead of addLast, you should use push or addFirst.
          - 0.5 / 1 : Toggle correct for redo
               - Same as above
          - 1 / 1 : Toggle correct for thickness FAB
     - 1 / 1 : Changing color correctly updates undo stack
     - 1 / 1 : Colorpicker shows correct color after color action is undone
     - 1 / 1 : Colorpicker shows correct color after color action is redone
- 1 / 1 : Code Organization, and Style
- **0 : Lateness and Other Adjustments**
     - Due: 06/03/2020 10:00 PM
     - Submitted: 06/02/2020 06:31 PM (on time)
     - 0 : Late days used on this assignment
     - 0 : Lateness deduction
     - 0 : Other adjustments

**Overall Comments:**
