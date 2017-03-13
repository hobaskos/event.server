(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventDialogController', EventDialogController);

    EventDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Event', 'User', 'Location', 'EventCategory', 'DataUtils'];

    function EventDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Event, User, Location, EventCategory, DataUtils) {
        var vm = this;

        vm.event = entity;
        vm.clear = clear;
        vm.save = save;
        vm.users = User.query();
        vm.locations = Location.query();
        vm.eventcategories = EventCategory.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.event.id !== null) {
                Event.update(vm.event, onSaveSuccess, onSaveError);
            } else {
                Event.save(vm.event, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:eventUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.setImage = function ($file, event) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        event.image = base64Data;
                        event.imageContentType = $file.type;
                    });
                });
            }
        };

    }
})();
