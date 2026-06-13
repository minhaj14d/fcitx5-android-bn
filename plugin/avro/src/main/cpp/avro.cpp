/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
#include "avro.h"

#include <fcitx-utils/key.h>
#include <fcitx-utils/keysym.h>
#include <fcitx/inputpanel.h>
#include <fcitx/userinterface.h>

namespace fcitx {

FCITX_ADDON_FACTORY(AvroFactory)

void AvroEngine::activate(const InputMethodEntry &, InputContextEvent &) { buffer_.clear(); }

void AvroEngine::reset(const InputMethodEntry &, InputContextEvent &) { buffer_.clear(); }

void AvroEngine::clearBuffer(InputContext *ic) {
    buffer_.clear();
    ic->inputPanel().setClientPreedit(Text());
    ic->updateUserInterface(UserInterfaceComponent::InputPanel);
}

void AvroEngine::updatePreedit(InputContext *ic) {
    if (buffer_.empty()) {
        ic->inputPanel().setClientPreedit(Text());
    } else {
        ic->inputPanel().setClientPreedit(Text(parser_.parse(buffer_)));
    }
    ic->updateUserInterface(UserInterfaceComponent::InputPanel);
}

bool AvroEngine::isProbhatEntry(const InputMethodEntry &entry) {
    return entry.uniqueName() == "probhat";
}

void AvroEngine::handleProbhatKeyEvent(KeyEvent &keyEvent) {
    if (keyEvent.isRelease()) {
        return;
    }

    const KeyStates modifiers{KeyState::Ctrl, KeyState::Alt};
    if (keyEvent.key().states() & modifiers) {
        return;
    }

    auto *ic = keyEvent.inputContext();
    const auto sym = keyEvent.key().sym();

    if (sym == FcitxKey_BackSpace) {
        keyEvent.filterAndAccept();
        return;
    }

    if (sym == FcitxKey_Return) {
        ic->commitString("\n");
        keyEvent.filterAndAccept();
        return;
    }

    if (sym == FcitxKey_space) {
        ic->commitString(" ");
        keyEvent.filterAndAccept();
        return;
    }

    if (keyEvent.key().isSimple()) {
        const auto text = Key::keySymToUTF8(sym);
        if (!text.empty()) {
            ic->commitString(text);
            keyEvent.filterAndAccept();
        }
    }
}

void AvroEngine::keyEvent(const InputMethodEntry &entry, KeyEvent &keyEvent) {
    if (isProbhatEntry(entry)) {
        handleProbhatKeyEvent(keyEvent);
        return;
    }

    if (keyEvent.isRelease()) {
        return;
    }

    const KeyStates modifiers{KeyState::Ctrl, KeyState::Alt};
    if (keyEvent.key().states() & modifiers) {
        return;
    }

    auto *ic = keyEvent.inputContext();
    const auto sym = keyEvent.key().sym();

    if (sym == FcitxKey_BackSpace) {
        if (buffer_.empty()) {
            return;
        }
        buffer_.pop_back();
        updatePreedit(ic);
        keyEvent.filterAndAccept();
        return;
    }

    if (sym == FcitxKey_Return || sym == FcitxKey_space) {
        if (!buffer_.empty()) {
            const auto commit = parser_.parse(buffer_);
            ic->commitString(commit + (sym == FcitxKey_space ? " " : "\n"));
            clearBuffer(ic);
            keyEvent.filterAndAccept();
        }
        return;
    }

    if (keyEvent.key().isSimple()) {
        const auto ch = static_cast<char>(sym);
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '`' || ch == '^' ||
            ch == ',' || ch == '.' || (ch >= '0' && ch <= '9')) {
            buffer_.push_back(ch);
            updatePreedit(ic);
            keyEvent.filterAndAccept();
            return;
        }
    }
}

} // namespace fcitx
