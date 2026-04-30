/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

(function () {
    const profiles = new Map();

    function escapeAttribute(text) {
        return (text || "").replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g,
            "&gt;");
    }

    function readProfilesFromSeeds() {
        document.querySelectorAll(".profile-account-seed").forEach(function (seed) {
            const rawProfileId = seed.dataset.profileId || "individual";
            const profileId = rawProfileId === "default-profile" ? "individual" : rawProfileId;
            const rawProfileName = seed.dataset.profileName || "Individual";
            const profileName = profileId === "individual" && rawProfileName === "Default Profile"
                ? "Individual"
                : rawProfileName;

            if (!profiles.has(profileId)) {
                profiles.set(profileId, {
                    profileId: profileId,
                    profileName: profileName
                });
            }
        });
    }

    function renderProfiles() {
        const listContainer = document.getElementById("profile-selection-list");
        if (!listContainer) {
            return;
        }

        if (profiles.size === 0) {
            const noAccountsText = document.getElementById("no-accounts-text-seed")?.dataset.value
                || "No consumer accounts";
            listContainer.innerHTML = "<b>" + escapeAttribute(noAccountsText) + "</b>";
            return;
        }

        let html = "";
        profiles.forEach(function (profile) {
            const inputId = "profile-" + escapeAttribute(profile.profileId);
            html += "<label for=\"" + inputId + "\">";
            html += "<input type=\"radio\" id=\"" + inputId + "\" name=\"optProfiles\" value=\""
                + escapeAttribute(profile.profileId) + "\" data-profile-name=\""
                + escapeAttribute(profile.profileName) + "\" onclick=\"setSelectedProfile()\" required/> ";
            html += escapeAttribute(profile.profileName);
            html += "</label><br/>";
        });

        listContainer.innerHTML = html;
    }

    function autoSelectProfileIfApplicable() {
        const rawPreSelectedProfileId = document.getElementById("pre-selected-profile-id-seed")?.dataset.value;
        const preSelectedProfileId = rawPreSelectedProfileId === "default-profile"
            ? "individual"
            : rawPreSelectedProfileId;

        if (preSelectedProfileId && profiles.has(preSelectedProfileId)) {
            const radios = document.querySelectorAll("input[name='optProfiles']");
            radios.forEach(function (radio) {
                if (radio.value === preSelectedProfileId) {
                    radio.checked = true;
                }
            });
            setSelectedProfile();
        }
    }

    function validateProfileSelection() {
        const selected = document.querySelector("input[name='optProfiles']:checked");
        if (!selected) {
            alert(document.getElementById("select-profile-text-seed")?.dataset.value || "Please select a profile");
            return false;
        }

        setSelectedProfile();
        return true;
    }

    window.setSelectedProfile = function () {
        const selected = document.querySelector("input[name='optProfiles']:checked");
        if (!selected) {
            return;
        }

        const selectedProfileIdInput = document.getElementById("selectedProfileId");
        const selectedProfileNameInput = document.getElementById("selectedProfileName");

        if (selectedProfileIdInput) {
            selectedProfileIdInput.value = selected.value;
        }

        if (selectedProfileNameInput) {
            selectedProfileNameInput.value = selected.dataset.profileName || "";
        }
    };

    window.showModal = function () {
        const modal = document.getElementById("cancelModal");
        if (modal) {
            modal.style.display = "block";
        }
    };

    window.closeModal = function () {
        const modal = document.getElementById("cancelModal");
        if (modal) {
            modal.style.display = "none";
        }
    };

    window.redirectFromProfileSelection = function () {
        const redirectUrl = document.getElementById("redirect-url-seed")?.dataset.value;
        const state = document.getElementById("state-seed")?.dataset.value;
        const errorDescription = "User skip the consent flow";

        if (!redirectUrl) {
            window.location.href = "retry.do?status=Error&statusMsg=User cancelled the consent flow";
            return;
        }

        if (state) {
            top.location = redirectUrl + "#error=access_denied&error_description=" +
                encodeURIComponent(errorDescription) + "&state=" + encodeURIComponent(state);
        } else {
            top.location = redirectUrl + "#error=access_denied&error_description=" +
                encodeURIComponent(errorDescription);
        }
    };

    window.addEventListener("DOMContentLoaded", function () {
        readProfilesFromSeeds();
        renderProfiles();
        autoSelectProfileIfApplicable();

        const form = document.getElementById("cds_profile_selection");
        if (form) {
            form.addEventListener("submit", function (event) {
                if (!validateProfileSelection()) {
                    event.preventDefault();
                }
            });
        }

        window.onclick = function (event) {
            const modal = document.getElementById("cancelModal");
            if (modal && event.target === modal) {
                closeModal();
            }
        };
    });
})();
