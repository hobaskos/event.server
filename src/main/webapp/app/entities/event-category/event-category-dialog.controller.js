(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventCategoryDialogController', EventCategoryDialogController);

    EventCategoryDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'EventCategory', 'Event'];

    function EventCategoryDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, EventCategory, Event) {
        var vm = this;

        vm.eventCategory = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.events = Event.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.eventCategory.id !== null) {
                EventCategory.update(vm.eventCategory, onSaveSuccess, onSaveError);
            } else {
                EventCategory.save(vm.eventCategory, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:eventCategoryUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        vm.setIcon = function ($file, eventCategory) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        eventCategory.icon = base64Data;
                        eventCategory.iconContentType = $file.type;
                    });
                });
            }
        };

    }
})();
