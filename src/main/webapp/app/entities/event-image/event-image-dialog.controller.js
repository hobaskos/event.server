(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventImageDialogController', EventImageDialogController);

    EventImageDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'EventImage', 'EventPoll'];

    function EventImageDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, EventImage, EventPoll) {
        var vm = this;

        vm.eventImage = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.eventpolls = EventPoll.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.eventImage.id !== null) {
                EventImage.update(vm.eventImage, onSaveSuccess, onSaveError);
            } else {
                EventImage.save(vm.eventImage, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:eventImageUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        vm.setFile = function ($file, eventImage) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        eventImage.file = base64Data;
                        eventImage.fileContentType = $file.type;
                    });
                });
            }
        };

    }
})();
