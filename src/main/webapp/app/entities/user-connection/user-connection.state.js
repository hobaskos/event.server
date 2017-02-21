(function() {
    'use strict';

    angular
        .module('backendApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('user-connection', {
            parent: 'entity',
            url: '/user-connection?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.userConnection.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/user-connection/user-connections.html',
                    controller: 'UserConnectionController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('userConnection');
                    $translatePartialLoader.addPart('userConnectionType');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('user-connection-detail', {
            parent: 'entity',
            url: '/user-connection/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.userConnection.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/user-connection/user-connection-detail.html',
                    controller: 'UserConnectionDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('userConnection');
                    $translatePartialLoader.addPart('userConnectionType');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'UserConnection', function($stateParams, UserConnection) {
                    return UserConnection.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'user-connection',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('user-connection-detail.edit', {
            parent: 'user-connection-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-connection/user-connection-dialog.html',
                    controller: 'UserConnectionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['UserConnection', function(UserConnection) {
                            return UserConnection.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('user-connection.new', {
            parent: 'user-connection',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-connection/user-connection-dialog.html',
                    controller: 'UserConnectionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                type: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('user-connection', null, { reload: 'user-connection' });
                }, function() {
                    $state.go('user-connection');
                });
            }]
        })
        .state('user-connection.edit', {
            parent: 'user-connection',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-connection/user-connection-dialog.html',
                    controller: 'UserConnectionDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['UserConnection', function(UserConnection) {
                            return UserConnection.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('user-connection', null, { reload: 'user-connection' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('user-connection.delete', {
            parent: 'user-connection',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/user-connection/user-connection-delete-dialog.html',
                    controller: 'UserConnectionDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['UserConnection', function(UserConnection) {
                            return UserConnection.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('user-connection', null, { reload: 'user-connection' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
