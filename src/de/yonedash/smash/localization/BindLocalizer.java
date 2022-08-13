package de.yonedash.smash.localization;

import de.yonedash.smash.Align;
import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.FontRenderer;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.config.KeyBind;

import java.awt.*;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

public class BindLocalizer implements Align {

    public static String getBindNameOrUnicode(KeyBind bind) {
        if (bind.getDevice() == KeyBind.Device.KEYBOARD)
            return getKeyNameOrUnicode(bind.getCode());
        else if (bind.getDevice() == KeyBind.Device.MOUSE)
            return getMouseButtonName(bind.getCode());
        else
            return null;
    }

    public static String getDeviceName(Scene scene, KeyBind.Device device) {
        return device != null ? scene.localize("device." + device.name().toLowerCase()) : null;
    }

    public static String getActualBindName(KeyBind bind) {
        if (bind.getDevice() == KeyBind.Device.KEYBOARD)
            return "[" + getActualKeyName(bind.getCode()) + "]";
        else if (bind.getDevice() == KeyBind.Device.MOUSE)
            return "[" + getMouseButtonName(bind.getCode()) + "]";
        else
            return null;
    }

    public static String getMouseButtonName(int code) {
        return "Mouse " + code;
    }

    public static String getKeyNameOrUnicode(int code) {
        String result = KeyEvent.getKeyText(code);
        boolean isUnknown = result.contains(Toolkit.getProperty("AWT.unknown", "Unknown"));
        return isUnknown ? "Key #" + code : result;
    }

    // Sloppy implementation, but enough for this use case

    // Dummy method which always returns the default value
    // This exists because I don't want to waste time refactoring
    // the AWT KeyEvent code for the key names
    private static String getProperty(String key, String defaultValue) {
        return defaultValue;
    }

    public static String getActualKeyName(int keyCode) {
        if (keyCode >= VK_0 && keyCode <= VK_9 ||
                keyCode >= VK_A && keyCode <= VK_Z) {
            return String.valueOf((char)keyCode);
        }

        switch(keyCode) {
            case VK_ENTER: return BindLocalizer.getProperty("AWT.enter", "Enter");
            case VK_BACK_SPACE: return BindLocalizer.getProperty("AWT.backSpace", "Backspace");
            case VK_TAB: return BindLocalizer.getProperty("AWT.tab", "Tab");
            case VK_CANCEL: return BindLocalizer.getProperty("AWT.cancel", "Cancel");
            case VK_CLEAR: return BindLocalizer.getProperty("AWT.clear", "Clear");
            case VK_COMPOSE: return BindLocalizer.getProperty("AWT.compose", "Compose");
            case VK_PAUSE: return BindLocalizer.getProperty("AWT.pause", "Pause");
            case VK_CAPS_LOCK: return BindLocalizer.getProperty("AWT.capsLock", "Caps Lock");
            case VK_ESCAPE: return BindLocalizer.getProperty("AWT.escape", "Escape");
            case VK_SPACE: return BindLocalizer.getProperty("AWT.space", "Space");
            case VK_PAGE_UP: return BindLocalizer.getProperty("AWT.pgup", "Page Up");
            case VK_PAGE_DOWN: return BindLocalizer.getProperty("AWT.pgdn", "Page Down");
            case VK_END: return BindLocalizer.getProperty("AWT.end", "End");
            case VK_HOME: return BindLocalizer.getProperty("AWT.home", "Home");
            case VK_LEFT: return BindLocalizer.getProperty("AWT.left", "Left");
            case VK_UP: return BindLocalizer.getProperty("AWT.up", "Up");
            case VK_RIGHT: return BindLocalizer.getProperty("AWT.right", "Right");
            case VK_DOWN: return BindLocalizer.getProperty("AWT.down", "Down");
            case VK_BEGIN: return BindLocalizer.getProperty("AWT.begin", "Begin");

            // modifiers
            case VK_SHIFT: return BindLocalizer.getProperty("AWT.shift", "Shift");
            case VK_CONTROL: return BindLocalizer.getProperty("AWT.control", "Control");
            case VK_ALT: return BindLocalizer.getProperty("AWT.alt", "Alt");
            case VK_META: return BindLocalizer.getProperty("AWT.meta", "Meta");
            case VK_ALT_GRAPH: return BindLocalizer.getProperty("AWT.altGraph", "Alt Graph");

            // punctuation
            case VK_COMMA: return BindLocalizer.getProperty("AWT.comma", "Comma");
            case VK_PERIOD: return BindLocalizer.getProperty("AWT.period", "Period");
            case VK_SLASH: return BindLocalizer.getProperty("AWT.slash", "Slash");
            case VK_SEMICOLON: return BindLocalizer.getProperty("AWT.semicolon", "Semicolon");
            case VK_EQUALS: return BindLocalizer.getProperty("AWT.equals", "Equals");
            case VK_OPEN_BRACKET: return BindLocalizer.getProperty("AWT.openBracket", "Open Bracket");
            case VK_BACK_SLASH: return BindLocalizer.getProperty("AWT.backSlash", "Back Slash");
            case VK_CLOSE_BRACKET: return BindLocalizer.getProperty("AWT.closeBracket", "Close Bracket");

            // numpad numeric keys handled below
            case VK_MULTIPLY: return BindLocalizer.getProperty("AWT.multiply", "NumPad *");
            case VK_ADD: return BindLocalizer.getProperty("AWT.add", "NumPad +");
            case VK_SEPARATOR: return BindLocalizer.getProperty("AWT.separator", "NumPad ,");
            case VK_SUBTRACT: return BindLocalizer.getProperty("AWT.subtract", "NumPad -");
            case VK_DECIMAL: return BindLocalizer.getProperty("AWT.decimal", "NumPad .");
            case VK_DIVIDE: return BindLocalizer.getProperty("AWT.divide", "NumPad /");
            case VK_DELETE: return BindLocalizer.getProperty("AWT.delete", "Delete");
            case VK_NUM_LOCK: return BindLocalizer.getProperty("AWT.numLock", "Num Lock");
            case VK_SCROLL_LOCK: return BindLocalizer.getProperty("AWT.scrollLock", "Scroll Lock");

            case VK_WINDOWS: return BindLocalizer.getProperty("AWT.windows", "Windows");
            case VK_CONTEXT_MENU: return BindLocalizer.getProperty("AWT.context", "Context Menu");

            case VK_F1: return BindLocalizer.getProperty("AWT.f1", "F1");
            case VK_F2: return BindLocalizer.getProperty("AWT.f2", "F2");
            case VK_F3: return BindLocalizer.getProperty("AWT.f3", "F3");
            case VK_F4: return BindLocalizer.getProperty("AWT.f4", "F4");
            case VK_F5: return BindLocalizer.getProperty("AWT.f5", "F5");
            case VK_F6: return BindLocalizer.getProperty("AWT.f6", "F6");
            case VK_F7: return BindLocalizer.getProperty("AWT.f7", "F7");
            case VK_F8: return BindLocalizer.getProperty("AWT.f8", "F8");
            case VK_F9: return BindLocalizer.getProperty("AWT.f9", "F9");
            case VK_F10: return BindLocalizer.getProperty("AWT.f10", "F10");
            case VK_F11: return BindLocalizer.getProperty("AWT.f11", "F11");
            case VK_F12: return BindLocalizer.getProperty("AWT.f12", "F12");
            case VK_F13: return BindLocalizer.getProperty("AWT.f13", "F13");
            case VK_F14: return BindLocalizer.getProperty("AWT.f14", "F14");
            case VK_F15: return BindLocalizer.getProperty("AWT.f15", "F15");
            case VK_F16: return BindLocalizer.getProperty("AWT.f16", "F16");
            case VK_F17: return BindLocalizer.getProperty("AWT.f17", "F17");
            case VK_F18: return BindLocalizer.getProperty("AWT.f18", "F18");
            case VK_F19: return BindLocalizer.getProperty("AWT.f19", "F19");
            case VK_F20: return BindLocalizer.getProperty("AWT.f20", "F20");
            case VK_F21: return BindLocalizer.getProperty("AWT.f21", "F21");
            case VK_F22: return BindLocalizer.getProperty("AWT.f22", "F22");
            case VK_F23: return BindLocalizer.getProperty("AWT.f23", "F23");
            case VK_F24: return BindLocalizer.getProperty("AWT.f24", "F24");

            case VK_PRINTSCREEN: return BindLocalizer.getProperty("AWT.printScreen", "Print Screen");
            case VK_INSERT: return BindLocalizer.getProperty("AWT.insert", "Insert");
            case VK_HELP: return BindLocalizer.getProperty("AWT.help", "Help");
            case VK_BACK_QUOTE: return BindLocalizer.getProperty("AWT.backQuote", "Back Quote");
            case VK_QUOTE: return BindLocalizer.getProperty("AWT.quote", "Quote");

            case VK_KP_UP: return BindLocalizer.getProperty("AWT.up", "Up");
            case VK_KP_DOWN: return BindLocalizer.getProperty("AWT.down", "Down");
            case VK_KP_LEFT: return BindLocalizer.getProperty("AWT.left", "Left");
            case VK_KP_RIGHT: return BindLocalizer.getProperty("AWT.right", "Right");

            case VK_DEAD_GRAVE: return BindLocalizer.getProperty("AWT.deadGrave", "Dead Grave");
            case VK_DEAD_ACUTE: return BindLocalizer.getProperty("AWT.deadAcute", "Dead Acute");
            case VK_DEAD_CIRCUMFLEX: return BindLocalizer.getProperty("AWT.deadCircumflex", "Dead Circumflex");
            case VK_DEAD_TILDE: return BindLocalizer.getProperty("AWT.deadTilde", "Dead Tilde");
            case VK_DEAD_MACRON: return BindLocalizer.getProperty("AWT.deadMacron", "Dead Macron");
            case VK_DEAD_BREVE: return BindLocalizer.getProperty("AWT.deadBreve", "Dead Breve");
            case VK_DEAD_ABOVEDOT: return BindLocalizer.getProperty("AWT.deadAboveDot", "Dead Above Dot");
            case VK_DEAD_DIAERESIS: return BindLocalizer.getProperty("AWT.deadDiaeresis", "Dead Diaeresis");
            case VK_DEAD_ABOVERING: return BindLocalizer.getProperty("AWT.deadAboveRing", "Dead Above Ring");
            case VK_DEAD_DOUBLEACUTE: return BindLocalizer.getProperty("AWT.deadDoubleAcute", "Dead Double Acute");
            case VK_DEAD_CARON: return BindLocalizer.getProperty("AWT.deadCaron", "Dead Caron");
            case VK_DEAD_CEDILLA: return BindLocalizer.getProperty("AWT.deadCedilla", "Dead Cedilla");
            case VK_DEAD_OGONEK: return BindLocalizer.getProperty("AWT.deadOgonek", "Dead Ogonek");
            case VK_DEAD_IOTA: return BindLocalizer.getProperty("AWT.deadIota", "Dead Iota");
            case VK_DEAD_VOICED_SOUND: return BindLocalizer.getProperty("AWT.deadVoicedSound", "Dead Voiced Sound");
            case VK_DEAD_SEMIVOICED_SOUND: return BindLocalizer.getProperty("AWT.deadSemivoicedSound", "Dead Semivoiced Sound");

            case VK_AMPERSAND: return BindLocalizer.getProperty("AWT.ampersand", "Ampersand");
            case VK_ASTERISK: return BindLocalizer.getProperty("AWT.asterisk", "Asterisk");
            case VK_QUOTEDBL: return BindLocalizer.getProperty("AWT.quoteDbl", "Double Quote");
            case VK_LESS: return BindLocalizer.getProperty("AWT.Less", "Less");
            case VK_GREATER: return BindLocalizer.getProperty("AWT.greater", "Greater");
            case VK_BRACELEFT: return BindLocalizer.getProperty("AWT.braceLeft", "Left Brace");
            case VK_BRACERIGHT: return BindLocalizer.getProperty("AWT.braceRight", "Right Brace");
            case VK_AT: return BindLocalizer.getProperty("AWT.at", "At");
            case VK_COLON: return BindLocalizer.getProperty("AWT.colon", "Colon");
            case VK_CIRCUMFLEX: return BindLocalizer.getProperty("AWT.circumflex", "Circumflex");
            case VK_DOLLAR: return BindLocalizer.getProperty("AWT.dollar", "Dollar");
            case VK_EURO_SIGN: return BindLocalizer.getProperty("AWT.euro", "Euro");
            case VK_EXCLAMATION_MARK: return BindLocalizer.getProperty("AWT.exclamationMark", "Exclamation Mark");
            case VK_INVERTED_EXCLAMATION_MARK: return BindLocalizer.getProperty("AWT.invertedExclamationMark", "Inverted Exclamation Mark");
            case VK_LEFT_PARENTHESIS: return BindLocalizer.getProperty("AWT.leftParenthesis", "Left Parenthesis");
            case VK_NUMBER_SIGN: return BindLocalizer.getProperty("AWT.numberSign", "Number Sign");
            case VK_MINUS: return BindLocalizer.getProperty("AWT.minus", "Minus");
            case VK_PLUS: return BindLocalizer.getProperty("AWT.plus", "Plus");
            case VK_RIGHT_PARENTHESIS: return BindLocalizer.getProperty("AWT.rightParenthesis", "Right Parenthesis");
            case VK_UNDERSCORE: return BindLocalizer.getProperty("AWT.underscore", "Underscore");

            case VK_FINAL: return BindLocalizer.getProperty("AWT.final", "Final");
            case VK_CONVERT: return BindLocalizer.getProperty("AWT.convert", "Convert");
            case VK_NONCONVERT: return BindLocalizer.getProperty("AWT.noconvert", "No Convert");
            case VK_ACCEPT: return BindLocalizer.getProperty("AWT.accept", "Accept");
            case VK_MODECHANGE: return BindLocalizer.getProperty("AWT.modechange", "Mode Change");
            case VK_KANA: return BindLocalizer.getProperty("AWT.kana", "Kana");
            case VK_KANJI: return BindLocalizer.getProperty("AWT.kanji", "Kanji");
            case VK_ALPHANUMERIC: return BindLocalizer.getProperty("AWT.alphanumeric", "Alphanumeric");
            case VK_KATAKANA: return BindLocalizer.getProperty("AWT.katakana", "Katakana");
            case VK_HIRAGANA: return BindLocalizer.getProperty("AWT.hiragana", "Hiragana");
            case VK_FULL_WIDTH: return BindLocalizer.getProperty("AWT.fullWidth", "Full-Width");
            case VK_HALF_WIDTH: return BindLocalizer.getProperty("AWT.halfWidth", "Half-Width");
            case VK_ROMAN_CHARACTERS: return BindLocalizer.getProperty("AWT.romanCharacters", "Roman Characters");
            case VK_ALL_CANDIDATES: return BindLocalizer.getProperty("AWT.allCandidates", "All Candidates");
            case VK_PREVIOUS_CANDIDATE: return BindLocalizer.getProperty("AWT.previousCandidate", "Previous Candidate");
            case VK_CODE_INPUT: return BindLocalizer.getProperty("AWT.codeInput", "Code Input");
            case VK_JAPANESE_KATAKANA: return BindLocalizer.getProperty("AWT.japaneseKatakana", "Japanese Katakana");
            case VK_JAPANESE_HIRAGANA: return BindLocalizer.getProperty("AWT.japaneseHiragana", "Japanese Hiragana");
            case VK_JAPANESE_ROMAN: return BindLocalizer.getProperty("AWT.japaneseRoman", "Japanese Roman");
            case VK_KANA_LOCK: return BindLocalizer.getProperty("AWT.kanaLock", "Kana Lock");
            case VK_INPUT_METHOD_ON_OFF: return BindLocalizer.getProperty("AWT.inputMethodOnOff", "Input Method On/Off");

            case VK_AGAIN: return BindLocalizer.getProperty("AWT.again", "Again");
            case VK_UNDO: return BindLocalizer.getProperty("AWT.undo", "Undo");
            case VK_COPY: return BindLocalizer.getProperty("AWT.copy", "Copy");
            case VK_PASTE: return BindLocalizer.getProperty("AWT.paste", "Paste");
            case VK_CUT: return BindLocalizer.getProperty("AWT.cut", "Cut");
            case VK_FIND: return BindLocalizer.getProperty("AWT.find", "Find");
            case VK_PROPS: return BindLocalizer.getProperty("AWT.props", "Props");
            case VK_STOP: return BindLocalizer.getProperty("AWT.stop", "Stop");
        }

        if (keyCode >= VK_NUMPAD0 && keyCode <= VK_NUMPAD9) {
            String numpad = BindLocalizer.getProperty("AWT.numpad", "NumPad");
            char c = (char)(keyCode - VK_NUMPAD0 + '0');
            return numpad + "-" + c;
        }

        if ((keyCode & 0x01000000) != 0) {
            return String.valueOf((char)(keyCode ^ 0x01000000 ));
        }
        return  "Key 0x" + Integer.toString(keyCode, 16);
    }

    public static BoundingBox drawHint(Graphics2D g2d, Scene scene, KeyBind keyBind, int x, int y, int height, int alignHorizontal, Color background, String override) {
        g2d.setFont(scene.instance.lexicon.arial.deriveFont((float) height * 0.6f));

        String text = getBindNameOrUnicode(keyBind);
        FontRenderer fontRenderer = scene.fontRenderer;
        Vec2D fontBounds = fontRenderer.bounds(g2d, text);

        int width = (int) (fontBounds.x + scene.scaleToDisplay(30.0));

        switch (alignHorizontal) {
            case CENTER -> x -= width / 2;
            case RIGHT -> x -= width;
        }

        g2d.setColor(background);
        g2d.fillRect(x, y, width, height);

        g2d.setColor(Color.DARK_GRAY);
        fontRenderer.drawStringAccurately(g2d, override != null ? override : text, x + width / 2, y + height / 2, Align.CENTER, Align.CENTER, false);

        return new BoundingBox(new Vec2D(x, y), new Vec2D(width, height));
    }

}

