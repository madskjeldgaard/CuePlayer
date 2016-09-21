
CuePlayerGUI {

  var cuePlayer, monitorInChannels, monitorOutChannels, options; 
  var <window, name, clock;
  var inputLevels,  cueTrigger, timer, <metronome, outputLevels, serverWindow;
  var font, titleFontSize, marginTop, <active = false;

  *new { arg cuePlayer, monitorInChannels = 2, monitorOutChannels = 8, options = ();
    ^super.newCopyArgs(cuePlayer, monitorInChannels, monitorOutChannels, options).init;
  }

  init {
    clock = cuePlayer.clock;
    name = cuePlayer.name ?? "Cue Player";

    this.setDefaultOptions;
    this.initStyleVariables;
    this.createMainWindow;
    this.createInputLevels;
    this.createCueTrigger;
    this.createTimer;
    this.createMetronome;
    this.createOutputLevels;
    this.createServerControls;
    this.registerShortcuts;

    active = true;
    window.front;
  }

  setDefaultOptions {
    options.monitorInOffset ?? { options.monitorInOffset = 0 };
    options.largeDisplay ?? { options.largeDisplay = false };
    options.left ?? { options.left = 1400 };
    options.top ?? { options.top = 650 };
  }

  initStyleVariables {
    font = "Lucida Grande";
    titleFontSize = 11;
    marginTop = 3;
  }

  createMainWindow {
    window = Window.new(name, Rect(options.left, options.top, 282, 330 + this.calculateLevelSumHeight + (marginTop*4)), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      inputLevels.clear;
      cueTrigger.clear;
      timer.clear;
      metronome.clear;
      outputLevels.clear;
      serverWindow.clear;
      cuePlayer.removeDependant(this);
      active = false;
    };
  }

  calculateLevelSumHeight { 
    var outHeightSum, inHeightSum, labelHeight = 20, outMeterHeight = 50, inMeterHeight = 20;
    labelHeight = labelHeight + 5;
    outMeterHeight = outMeterHeight;
    inMeterHeight = inMeterHeight + 5;
    outHeightSum = ((monitorOutChannels/8).roundUp(1))*(outMeterHeight+labelHeight);
    inHeightSum = monitorInChannels*inMeterHeight;
    ^(outHeightSum + inHeightSum);
  }

  registerShortcuts {
    window.view.keyDownAction = { 
      arg view, char, modifiers, unicode, keycode; 
      /* [char, modifiers, unicode, keycode].postln; */ 
      switch(unicode)
      {32} { cueTrigger.trigButton.doAction(0) } //space
      {109} {} // m
      {77} {} //M
      {112} {} //p
    }; 
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(Font(font, titleFontSize));
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  /* -------- */

  createInputLevels {
    inputLevels = InputMetersCP(
      window, 
      options: (
        monitorInChannels: monitorInChannels, 
        monitorInOffset: options.monitorInOffset, 
        font: Font(font, titleFontSize)
      )
    );
  }

  createCueTrigger {
    this.createLabel("", 282, marginTop);
    cueTrigger = CueTriggerCP(window, options: (largeDisplay: options.largeDisplay));
    cueTrigger.trigButton.action = { var cueNum;
        cueNum = cuePlayer.next;
        if (timer.isPlaying.not) {timer.play; timer.pauseButton.value_(1)};
    };
    cueTrigger.cueNumberBox.action = { arg box;
      cuePlayer.current = box.value.abs;
      timer.stop;
      if(box.value == 0){timer.stop; timer.cursecs_(0)};
      if (options.largeDisplay, { cueTrigger.largeCueNumberDisplay.string = box.value })
    };
  }

  createTimer {
    this.createLabel("", 282, marginTop);
    timer = TimerCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
  }

  createMetronome{
    this.createLabel("", 282, marginTop);
    metronome = MetronomeCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
  }

  createOutputLevels{
    this.createLabel("", 282, marginTop);
    outputLevels = OutputMetersCP(
      window, 
      options: (
        monitorOutChannels: monitorOutChannels, 
        font: Font(font, titleFontSize)
      )
    );
  }

  createServerControls {
    serverWindow = ServerWindowCP(window, options: (tempoClock: clock, font: Font(font, titleFontSize)));
  }

  /* Handle Events from Dependants */

  update { arg theChanged, message;
    switch (message)
    {\current}
    {this.setCurrent(theChanged.current)}
    {\tempo}
    {metronome.bpm = theChanged.clock.tempo*60}
  }

  setCurrent { arg val;
    cueTrigger.setCurrent(val);
  }

}
